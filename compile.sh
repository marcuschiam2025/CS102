#!/bin/sh
# Compiles the app in src/ into classes/
# (src/RenameToYourUsername is the exam starter code, not part of the app)
cd "$(dirname "$0")"
javac -d classes $(find src -name "*.java" -not -path "src/RenameToYourUsername/*")
