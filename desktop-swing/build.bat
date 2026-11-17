@echo off
setlocal enabledelayedexpansion

echo ====================================================
echo  Building ChronoTrack Desktop Swing (Java)
echo ====================================================

:: Locate javac
set JAVAC_CMD=javac
if exist "%JAVA_HOME%\bin\javac.exe" (
    set JAVAC_CMD="%JAVA_HOME%\bin\javac.exe"
) else (
    for /d %%D in ("C:\Program Files\Java\jdk*") do (
        if exist "%%D\bin\javac.exe" (
            set JAVAC_CMD="%%D\bin\javac.exe"
        )
    )
)

echo Using compiler: !JAVAC_CMD!

:: Prepare output folder
if not exist "target\classes" (
    mkdir "target\classes"
)

:: Find and compile all Java sources
echo Collecting Java source files...
dir /s /b "src\main\java\*.java" > sources.tmp

echo Compiling sources to target\classes...
!JAVAC_CMD! -encoding UTF-8 -d "target\classes" @sources.tmp
set BUILD_STATUS=%ERRORLEVEL%
del sources.tmp

if %BUILD_STATUS% equ 0 (
    echo [SUCCESS] ChronoTrack Desktop Swing built successfully!
    echo Run "run.bat" to start the application.
) else (
    echo [ERROR] Build failed with exit code %BUILD_STATUS%.
)

exit /b %BUILD_STATUS%
