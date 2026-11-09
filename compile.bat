@echo off
rem Compiles the app in src\ into classes\
rem javac finds every class Main and GradingMenu use by itself
cd /d "%~dp0"
javac -d classes -sourcepath src src\app\Main.java src\app\GradingMenu.java
