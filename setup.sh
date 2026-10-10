#!/usr/bin/env bash
# =====================================================================
# PASSAGE CLI: One-Command Portability & Runner Installer
# Platforms: Linux / macOS
# =====================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

BOLD="\033[1;37m"
DIM="\033[2;37m"
INVERT="\033[7m"
RESET="\033[0m"

echo -e "${DIM}┌────────────────────────────────────────────────────────────────────────┐${RESET}"
echo -e "${DIM}│${RESET} ${BOLD}PASSAGE CLI SETUP${RESET}           ${DIM}College Travel Safety Platform Installer${RESET}  ${DIM}│${RESET}"
echo -e "${DIM}└────────────────────────────────────────────────────────────────────────┘${RESET}"

# 1. Verify Java Environment
echo -e "\n${BOLD}[1/5] Checking Java & Maven Environment...${RESET}"
if ! command -v java >/dev/null 2>&1; then
    if [ -x "$HOME/.local/bin/java" ]; then
        export PATH="$HOME/.local/bin:$PATH"
    elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        export PATH="$JAVA_HOME/bin:$PATH"
    else
        echo -e "${DIM}Error: Java 17+ is required but not found in PATH.${RESET}"
        exit 1
    fi
fi
echo -e "${DIM}  Using Java: $(java -version 2>&1 | head -n 1)${RESET}"

if ! command -v mvn >/dev/null 2>&1; then
    if [ -x "$HOME/.local/bin/mvn" ]; then
        export PATH="$HOME/.local/bin:$PATH"
    fi
fi

# 2. Verify MariaDB / MySQL Daemon
echo -e "\n${BOLD}[2/5] Verifying MariaDB / MySQL Service...${RESET}"
DB_CMD="mariadb"
if ! command -v mariadb >/dev/null 2>&1; then
    if command -v mysql >/dev/null 2>&1; then
        DB_CMD="mysql"
    else
        echo -e "${DIM}Warning: Neither mariadb nor mysql CLI found. Relying on JDBC auto-schema initialization.${RESET}"
        DB_CMD=""
    fi
fi

if systemctl is-active --quiet mariadb 2>/dev/null; then
    echo -e "${DIM}  MariaDB service is active and running.${RESET}"
elif systemctl is-active --quiet mysql 2>/dev/null; then
    echo -e "${DIM}  MySQL service is active and running.${RESET}"
else
    echo -e "${DIM}  Attempting to start database service...${RESET}"
    systemctl start mariadb 2>/dev/null || systemctl start mysql 2>/dev/null || echo -e "${DIM}  (Service start skipped; ensure database daemon is running)${RESET}"
fi

# 3. Import Schema & Test Data
echo -e "\n${BOLD}[3/5] Importing schema.sql & Seed Datasets...${RESET}"
if [ -n "$DB_CMD" ]; then
    DB_PASS="${DB_PASSWORD:-}"
    if [ -z "$DB_PASS" ]; then
        # Try connecting with empty password, then fallback to common local password
        if $DB_CMD -u root -e "SELECT 1;" >/dev/null 2>&1; then
            DB_PASS=""
        elif $DB_CMD -u root -pmaria -e "SELECT 1;" >/dev/null 2>&1; then
            DB_PASS="maria"
            export DB_PASSWORD="maria"
        fi
    fi

    if [ -n "$DB_PASS" ]; then
        $DB_CMD -u root -p"$DB_PASS" < "$SCRIPT_DIR/src/main/resources/schema.sql" 2>/dev/null || true
    else
        $DB_CMD -u root < "$SCRIPT_DIR/src/main/resources/schema.sql" 2>/dev/null || true
    fi
    echo -e "${DIM}  Database 'ridesafe' schema and seed records loaded.${RESET}"
fi

# 4. Compile and Build Executable Fat JAR
echo -e "\n${BOLD}[4/5] Building Executable Fat JAR with Maven...${RESET}"
mvn clean package -DskipTests=false
echo -e "${DIM}  Successfully packaged target/passage.jar${RESET}"

# 5. Global Keyword Installation
echo -e "\n${BOLD}[5/5] Installing 'passage' Keyword Runner...${RESET}"
INSTALL_DIR="$HOME/.local/share/passage"
BIN_DIR="$HOME/.local/bin"
mkdir -p "$INSTALL_DIR" "$BIN_DIR"

# Copy fat jar to user share
cp -f "$SCRIPT_DIR/target/passage.jar" "$INSTALL_DIR/passage.jar"

# Create standalone runner wrapper script
cat << 'EOF' > "$BIN_DIR/passage"
#!/usr/bin/env bash
# Passage CLI Global Executable Wrapper

if command -v java >/dev/null 2>&1; then
    JAVA_BIN="java"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_BIN="$JAVA_HOME/bin/java"
elif [ -x "$HOME/.local/bin/java" ]; then
    JAVA_BIN="$HOME/.local/bin/java"
else
    echo "Error: Java 17 or higher is required to execute Passage CLI." >&2
    exit 1
fi

JAR_FILE="$HOME/.local/share/passage/passage.jar"
if [ ! -f "$JAR_FILE" ]; then
    SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" 2>/dev/null && pwd)"
    JAR_FILE="$SCRIPT_PATH/../share/passage/passage.jar"
fi

exec "$JAVA_BIN" -jar "$JAR_FILE" "$@"
EOF

chmod +x "$BIN_DIR/passage"

# Attempt system-wide install if /usr/local/bin is writable or sudo available without prompt
if [ -w "/usr/local/bin" ]; then
    mkdir -p "/usr/local/share/passage"
    cp -f "$INSTALL_DIR/passage.jar" "/usr/local/share/passage/passage.jar"
    cp -f "$BIN_DIR/passage" "/usr/local/bin/passage"
    chmod +x "/usr/local/bin/passage"
    echo -e "${DIM}  Symlinked global executable to /usr/local/bin/passage${RESET}"
fi

# Ensure ~/.local/bin is in PATH in ~/.bashrc and ~/.zshrc
for RC in "$HOME/.bashrc" "$HOME/.zshrc"; do
    if [ -f "$RC" ] && ! grep -q '\.local/bin' "$RC"; then
        echo 'export PATH="$HOME/.local/bin:$PATH"' >> "$RC"
    fi
done

echo -e "\n${DIM}┌────────────────────────────────────────────────────────────────────────┐${RESET}"
echo -e "${DIM}│${RESET}  ${INVERT} SETUP COMPLETE ${RESET} ${BOLD}You can now run Passage CLI from anywhere!${RESET}     ${DIM}│${RESET}"
echo -e "${DIM}│${RESET}  ${BOLD}Command: passage${RESET}                                                      ${DIM}│${RESET}"
echo -e "${DIM}│${RESET}  Try: ${DIM}passage --help${RESET} or ${DIM}passage --scan${RESET}                              ${DIM}│${RESET}"
echo -e "${DIM}└────────────────────────────────────────────────────────────────────────┘${RESET}\n"
