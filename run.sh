#!/bin/sh
# Runs the application (compile with compile.sh first)
cd "$(dirname "$0")" || exit 1

# Java on Windows separates classpath entries with ";", elsewhere with ":"
case "$(uname -s)" in
    CYGWIN* | MINGW* | MSYS*) SEP=";" ;;
    *) SEP=":" ;;
esac

java -cp "classes${SEP}lib/*" app.Main
