@echo off
REM =====================================================================
REM PASSAGE CLI: Windows Setup Script & Keyword Runner Installer
REM =====================================================================

echo [1/4] Checking Database and Importing schema.sql...
where mariadb >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mariadb -u root < "%~dp0src\main\resources\schema.sql"
    goto :db_done
)

where mysql >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mysql -u root < "%~dp0src\main\resources\schema.sql"
    goto :db_done
)
echo Notice: MySQL/MariaDB CLI not found in PATH. Schema will auto-initialize via pure JDBC on startup.

:db_done
echo.
echo [2/4] Building Executable Fat JAR with Maven...
call mvn clean package -DskipTests=false
if %ERRORLEVEL% neq 0 (
    echo Build failed. Please verify Maven and JDK 17+ installation.
    exit /b 1
)

echo.
echo [3/4] Installing Passage runner files into %USERPROFILE%\.passage...
if not exist "%USERPROFILE%\.passage" mkdir "%USERPROFILE%\.passage"
copy /Y "%~dp0target\passage.jar" "%USERPROFILE%\.passage\passage.jar" >nul

REM Create passage.cmd wrapper
(
echo @echo off
echo java -jar "%USERPROFILE%\.passage\passage.jar" %%*
) > "%USERPROFILE%\.passage\passage.cmd"

echo.
echo [4/4] Registering %%USERPROFILE%%\.passage to user PATH...
echo %PATH% | findstr /I /C:"%USERPROFILE%\.passage" >nul
if %ERRORLEVEL% neq 0 (
    setx PATH "%PATH%;%USERPROFILE%\.passage" >nul 2>&1
)

echo.
echo ========================================================================
echo   SETUP COMPLETE! Passage CLI is now globally installed.
echo   Type "passage" in any new command prompt window to launch.
echo   Examples:
echo     passage
echo     passage --scan
echo     passage --help
echo ========================================================================
echo.
