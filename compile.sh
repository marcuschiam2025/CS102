#!/bin/sh
# Compiles the app in src/ into src/classes/
# (src/RenameToYourUsername is the exam starter code, not part of the app)
cd "$(dirname "$0")"
javac -d src/classes $(find src -name "*.java" -not -path "src/RenameToYourUsername/*")
