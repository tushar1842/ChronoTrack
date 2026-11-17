@echo off
setlocal enabledelayedexpansion

echo ====================================================
echo  Launching ChronoTrack Desktop Swing Dashboard
echo ====================================================

if not exist "target\classes\com\chronotrack\Main.class" (
    echo Compiled classes not found. Triggering build...
    call build.bat
    if errorlevel 1 (
        echo Build failed. Exiting.
        exit /b 1
    )
)

:: Locate java
set JAVA_CMD=java
if exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
) else (
    for /d %%D in ("C:\Program Files\Java\jdk*") do (
        if exist "%%D\bin\java.exe" (
            set JAVA_CMD="%%D\bin\java.exe"
        )
    )
)

echo Starting application...
!JAVA_CMD! -cp "target\classes" com.chronotrack.Main %*
