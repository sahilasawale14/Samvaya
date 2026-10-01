@echo off

echo ===================================================
echo   SAMVAYA SOCIETY MANAGEMENT SYSTEM - FRONTEND
echo   Starting Frontend on Port 3000
echo ===================================================
echo.

cd /d "%~dp0frontend"

echo Starting frontend server...
echo.

npx -y serve -l 3000

pause
