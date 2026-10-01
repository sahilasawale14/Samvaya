@echo off
echo ===================================================
echo   SAMVAYA SOCIETY MANAGEMENT SYSTEM - BACKEND
echo   Spring Boot REST API (Port 8080)
echo ===================================================

if not defined JAVA_HOME (
    if exist "%USERPROFILE%\.jdks\temurin-17" (
        set "JAVA_HOME=%USERPROFILE%\.jdks\temurin-17"
    ) else if exist "%USERPROFILE%\.jdks\temurin-25.0.4.1" (
        set "JAVA_HOME=%USERPROFILE%\.jdks\temurin-25.0.4.1"
    )
)

if exist "%USERPROFILE%\AppData\Local\Programs\apache-maven-3.9.6\bin" (
    set "PATH=%USERPROFILE%\AppData\Local\Programs\apache-maven-3.9.6\bin;%JAVA_HOME%\bin;%PATH%"
) else if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

cd /d "%~dp0backend"
echo Active Java Environment:
java -version
echo.
echo Starting Spring Boot REST Backend on port 8080...
if exist "mvnw.cmd" (
    call mvnw.cmd spring-boot:run
) else (
    call mvn spring-boot:run
)
pause

