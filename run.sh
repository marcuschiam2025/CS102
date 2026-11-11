#!/bin/sh
# Runs the app (run compile.sh first)
cd "$(dirname "$0")"
java -cp src/classes app.Main
