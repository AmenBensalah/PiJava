@echo off
cd C:\Users\ghaie\IdeaProjects\E-sportify
mvn.cmd clean compile -DskipTests 2>&1 | findstr /R "ERROR \[ERROR\]"
if %ERRORLEVEL% EQU 0 (
    echo.
    echo COMPILATION ERRORS FOUND - check the log above
    exit /B 1
) else (
    echo.
    echo BUILD SUCCESSFUL
    exit /B 0
)
