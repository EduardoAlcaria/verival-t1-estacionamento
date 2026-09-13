#!/usr/bin/env bash
set -e

BASE="$(cd "$(dirname "$0")" && pwd)"
JUNIT="$BASE/lib/junit-platform-console-standalone.jar"
JUNIT_URL="https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar"
OUT="$BASE/target/classes"
TEST_OUT="$BASE/target/test-classes"

if [ ! -f "$JUNIT" ]; then
    echo "Baixando JUnit Platform Console Standalone..."
    mkdir -p "$BASE/lib"
    curl -sL -o "$JUNIT" "$JUNIT_URL"
fi

rm -rf "$BASE/target"
mkdir -p "$OUT" "$TEST_OUT"

javac -encoding UTF-8 -d "$OUT" $(find "$BASE/src/main/java" -name '*.java')
javac -encoding UTF-8 -cp "$OUT:$JUNIT" -d "$TEST_OUT" $(find "$BASE/src/test/java" -name '*.java')

java -jar "$JUNIT" execute \
    --class-path "$OUT:$TEST_OUT" \
    --scan-class-path \
    --details=tree \
    --disable-ansi-colors
