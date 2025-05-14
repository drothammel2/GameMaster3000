@echo off
echo Kompiliere das Projekt...
javac -d bin src\*.java
if %errorlevel% neq 0 (
    echo Fehler beim Kompilieren.
    exit /b %errorlevel%
)
echo Starte das Projekt...
java -cp bin Main
