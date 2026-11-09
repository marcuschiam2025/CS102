#!/bin/sh
# Compiles all Java source files in src/ into classes/
cd "$(dirname "$0")" || exit 1

# src/RenameToYourUsername holds the exam's starter code (student code,
# not part of this application), so it is not compiled
mkdir -p classes
javac -d classes -cp "lib/*" \
    $(find src -name "*.java" -not -path "src/RenameToYourUsername/*")
