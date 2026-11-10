@echo off
rem Compiles the app in src\ into classes\
rem (src\RenameToYourUsername is the exam starter code, not part of the app)
cd /d "%~dp0"
javac -d classes -sourcepath src src\app\Main.java src\app\GradingMenu.java src\model\Tester.java
