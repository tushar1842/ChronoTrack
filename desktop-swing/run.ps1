# ChronoTrack Desktop Swing PowerShell Runner
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host " Launching ChronoTrack Desktop Swing Dashboard" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# Find Java compiler & runtime
$javaBin = "java"
$javacBin = "javac"

if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $javaBin = "$env:JAVA_HOME\bin\java.exe"
    $javacBin = "$env:JAVA_HOME\bin\javac.exe"
} else {
    $jdkDirs = Get-ChildItem "C:\Program Files\Java\jdk*" -ErrorAction SilentlyContinue | Sort-Object Name -Descending
    if ($jdkDirs -and $jdkDirs.Count -gt 0) {
        $javaBin = "$($jdkDirs[0].FullName)\bin\java.exe"
        $javacBin = "$($jdkDirs[0].FullName)\bin\javac.exe"
    }
}

if (!(Test-Path "target\classes\com\chronotrack\Main.class")) {
    Write-Host "Compiling Java sources..." -ForegroundColor Yellow
    if (!(Test-Path "target\classes")) {
        New-Item -ItemType Directory -Force -Path "target\classes" | Out-Null
    }
    $sources = Get-ChildItem -Recurse -Path "src\main\java" -Filter "*.java" | Select-Object -ExpandProperty FullName
    & $javacBin -encoding UTF-8 -d "target\classes" $sources
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Compilation failed." -ForegroundColor Red
        exit $LASTEXITCODE
    }
    Write-Host "Build complete!" -ForegroundColor Green
}

Write-Host "Starting ChronoTrack Swing UI..." -ForegroundColor Green
& $javaBin -cp "target\classes" com.chronotrack.Main @args
