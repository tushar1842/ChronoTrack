#!/usr/bin/env bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

echo "===================================================="
echo " ChronoTrack Desktop Swing"
echo "===================================================="

if [ ! -f "target/classes/com/chronotrack/Main.class" ]; then
    echo "Compiling..."
    mkdir -p target/classes
    find src/main/java -name "*.java" > sources.tmp
    javac -encoding UTF-8 -d target/classes @sources.tmp
    rm sources.tmp
fi

echo "Running..."
java -cp "target/classes" com.chronotrack.Main "$@"
