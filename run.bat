@echo off
rem Runs the app (run compile.bat first)
cd /d "%~dp0"
java -cp classes app.Main
