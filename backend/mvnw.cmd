@echo off
setlocal

if not defined JAVA_HOME (
    if exist "%USERPROFILE%\.jdks\temurin-17" (
        set "JAVA_HOME=%USERPROFILE%\.jdks\temurin-17"
    ) else if exist "%USERPROFILE%\.jdks\temurin-25.0.4.1" (
        set "JAVA_HOME=%USERPROFILE%\.jdks\temurin-25.0.4.1"
    )
)

set "MAVEN_HOME=%USERPROFILE%\AppData\Local\Programs\apache-maven-3.9.6"
if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    set "PATH=%MAVEN_HOME%\bin;%JAVA_HOME%\bin;%PATH%"
    "%MAVEN_HOME%\bin\mvn.cmd" %*
) else (
    if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"
    mvn %*
)

