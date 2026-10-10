# Passage CLI (College Travel Safety Platform)

> Production-grade terminal-based college travel safety platform backed by MariaDB/MySQL using pure JDBC, featuring an ultra-clean, minimalist monochrome developer UI.

---

## ✦ Key Highlights

- **Pure JDBC Database Persistence**: Thread-safe connection management with zero ORM overhead, auto-reconnect, and dual driver support (`mariadb-java-client` and `mysql-connector-j`).
- **Minimalist Monochrome Terminal UI**: High-contrast, distraction-free aesthetic inspired by modern developer CLI tools (Claude Code, Stripe CLI, GitHub CLI). Strictly monochrome: bold white headers, muted borders, and inverted video badges (`[ DELAY ALERT ]`, `[ IN PROGRESS ]`).
- **5-Table Relational Schema**: Normalized MariaDB/MySQL database (`ridesafe`) with cascade integrity and timestamp audit logging.
- **Automated Safety Watchdog Engine**: Real-time query scanning for overdue in-progress trips (`NOW() > expected_arrival_time`) with automatic alert generation and deduplication protection.
- **Global Keyword Runner**: Standalone executable Fat JAR packaged with `maven-shade-plugin`, runnable globally via the keyword `passage`.

---

## ✦ Database Architecture (`ridesafe`)

```
   ┌────────────────┐       1 : N       ┌────────────────────┐
   │    STUDENTS    │───────────────────│  TRUSTED_CONTACTS  │
   └────────────────┘                   └────────────────────┘
           │ 1                                     │ 1
           │                                       │
           │ N                                     │ N
   ┌────────────────┐       1 : N       ┌────────────────────┐
   │    JOURNEYS    │───────────────────│   SAFETY_ALERTS    │
   └────────────────┘                   └────────────────────┘
           │ 1                                     ▲
           │                                       │
           │ N                                     │
   ┌────────────────┐                              │
   │ JOURNEY_UPDATES│                              │
   └────────────────┘                              │
           └───────────────────────────────────────┘
```

### Table Definitions

| Table | Primary Key | Foreign Keys | Description |
|---|---|---|---|
| `STUDENTS` | `student_id` | - | Student profile with university email and direct phone |
| `TRUSTED_CONTACTS` | `contact_id` | `student_id` -> `STUDENTS` | Registered guardians, parents, or roommates for emergency notifications |
| `JOURNEYS` | `journey_id` | `student_id` -> `STUDENTS` | Travel itinerary with departure, ETA, actual arrival, and status enum |
| `JOURNEY_UPDATES` | `update_id` | `journey_id` -> `JOURNEYS` | Chronological checkpoints, transit remarks, and audit trail |
| `SAFETY_ALERTS` | `alert_id` | `journey_id`, `contact_id` | Deduplicated delay warnings (`DELAY_ALERT`) and arrival notifications (`JOURNEY_COMPLETION`) |

---

## ✦ Installation & Quickstart

### Linux & macOS

Run the automated one-command setup script:

```bash
chmod +x setup.sh
./setup.sh
```

The script will:
1. Verify MariaDB/MySQL service status.
2. Import `src/main/resources/schema.sql` and load 3 complete lifecycle test scenarios.
3. Build the standalone executable `target/passage.jar` via `mvn clean package`.
4. Install the runner script to `~/.local/bin/passage` (and `/usr/local/bin/passage` if writable).

Now run the CLI anywhere in your terminal:
```bash
passage
```

### Windows

Run the batch setup script:

```bat
setup.bat
```

This imports the schema, builds `passage.jar`, creates `%USERPROFILE%\.passage\passage.cmd`, and adds it to your user `PATH`.

---

## ✦ CLI Usage & Command Reference

### Interactive Monochrome UI (Default)

Simply type `passage` to enter the interactive console:

```
┌────────────────────────────────────────────────────────────────────────┐
│ PASSAGE CLI  v1.0.0             College Travel Safety Platform         │
│ Minimalist Monochrome Interface      Powered by MariaDB / Pure JDBC    │
└────────────────────────────────────────────────────────────────────────┘

─── MAIN NAVIGATION ────────────────────────────────────────────────────
  [1] Register New Student           Enroll student with contact details
  [2] Manage Trusted Contacts        Add, inspect or remove safety contacts
  [3] Start / Schedule Journey       Log new trip (PLANNED / IN_PROGRESS)
  [4] Update Journey & Checkpoints   Add checkpoint, complete or cancel trip
  [5] Safety Watchdog Engine         Scan active trips for overdue delays
  [6] View Safety Alerts             Review & acknowledge safety notifications
  [7] Student Journey History        Audit complete history with nested trees
  [8] Active Journeys Monitor        Live tabular view of all underway trips
  [9] Purge All Database Records     Delete all students, trips & alerts
  [0] Exit Application
────────────────────────────────────────────────────────────────────────
passage ❯ _
```

### Headless CLI Flags

Passage CLI also supports headless non-interactive execution for background cron tasks and monitoring:

```bash
# Execute Safety Watchdog scan on overdue journeys
passage --scan

# Monitor currently active journeys in tabular format
passage --active

# Inspect all dispatched and pending safety alerts
passage --alerts

# Retrieve nested history tree for student ID
passage --history 1

# Delete all data across all tables (truncate)
passage --clean

# Re-seed database with all 3 test lifecycle scenarios
passage --seed

# Display help dialogue
passage --help
```

---

## ✦ Safety Watchdog Algorithm & Deduplication

When `runSafetyScan()` is triggered:
1. **Query**: Selects all trips where:
   ```sql
   journey_status = 'IN_PROGRESS' AND NOW() > expected_arrival_time
   ```
2. **Contact Traversal**: For each overdue journey, queries all registered `TRUSTED_CONTACTS` for the student.
3. **Deduplication**: Checks if a `DELAY_ALERT` already exists:
   ```sql
   SELECT 1 FROM SAFETY_ALERTS 
   WHERE journey_id = ? AND contact_id = ? AND alert_type = 'DELAY_ALERT'
   ```
   If already notified, the contact is safely skipped.
4. **Dispatch**: For unalerted contacts, inserts a new `SAFETY_ALERTS` row (`alert_type = 'DELAY_ALERT'`, `alert_status = 'SENT'`).
5. **Audit Checkpoint**: Inserts a log remark into `JOURNEY_UPDATES` noting that the safety watchdog detected the delay and notified emergency contacts.

---

## ✦ Project Structure

```
passage-cli/
├── pom.xml                               # Maven build configuration (Java 21 + Shade Plugin)
├── setup.sh                              # Linux/macOS one-command runner installer
├── setup.bat                             # Windows setup installer
├── README.md                             # Documentation
└── src/
    ├── main/
    │   ├── resources/
    │   │   ├── db.properties             # Database credentials configuration
    │   │   └── schema.sql                # 5-table schema + 3 seed datasets
    │   └── java/com/passage/
    │       ├── Main.java                 # Entry point with CLI command router
    │       ├── config/
    │       │   └── DBConnection.java     # Thread-safe JDBC connection manager
    │       ├── model/                    # Data models & Enums
    │       │   ├── Student.java
    │       │   ├── TrustedContact.java
    │       │   ├── Journey.java
    │       │   ├── JourneyStatus.java
    │       │   ├── JourneyUpdate.java
    │       │   ├── SafetyAlert.java
    │       │   ├── AlertType.java
    │       │   ├── AlertStatus.java
    │       │   └── JourneyDetails.java
    │       ├── dao/                      # Pure JDBC Data Access Objects
    │       │   ├── StudentDao.java
    │       │   ├── TrustedContactDao.java
    │       │   ├── JourneyDao.java
    │       │   ├── JourneyUpdateDao.java
    │       │   └── SafetyAlertDao.java
    │       ├── service/                  # Business Logic & Watchdog
    │       │   ├── StudentService.java
    │       │   ├── JourneyService.java
    │       │   ├── SafetyWatchdogService.java
    │       │   └── DatabaseInitializer.java
    │       └── ui/                       # Monochrome Terminal UI
    │           ├── MonochromeTheme.java  # High-contrast monochrome ANSI styling
    │           ├── AsciiTable.java       # ANSI-safe Unicode table renderer
    │           └── TerminalUI.java       # Interactive console controller
    └── test/java/com/passage/
        └── PassageCoreTest.java          # JUnit 5 integration test suite
```
