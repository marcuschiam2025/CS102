#!/bin/sh
# Compiles all Java source files in src/ into classes/
cd "$(dirname "$0")" || exit 1

mkdir -p classes
javac -d classes -cp "lib/*" $(find src -name "*.java")
