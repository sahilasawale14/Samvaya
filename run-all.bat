@echo off
echo ===================================================
echo   SAMVAYA SOCIETY MANAGEMENT SYSTEM - FULL STACK
echo   Launching Backend (Port 8080) + Frontend (Port 3000)
echo ===================================================
echo.
echo 1. Starting Backend REST API...
start "SAMVAYA Backend" cmd /k "%~dp0run-backend.bat"

echo 2. Waiting 5 seconds for Spring Boot initialization...
timeout /t 5 >nul

echo 3. Starting Frontend Web Server...
start "SAMVAYA Frontend" cmd /k "%~dp0run-frontend.bat"

echo.
echo ===================================================
echo   Both services have been launched!
echo   - Frontend: http://localhost:3000
echo   - Backend:  http://localhost:8080/api
echo ===================================================
