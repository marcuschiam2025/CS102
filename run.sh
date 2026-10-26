#!/bin/sh
# Runs the application (compile with compile.sh first)
cd "$(dirname "$0")" || exit 1

java -cp "classes:lib/*" app.Main
