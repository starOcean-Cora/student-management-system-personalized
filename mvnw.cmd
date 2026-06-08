@echo off

set "JAVA_HOME=D:\IDEA\JDK"
set "MVN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9"

if not exist "%MVN_HOME%\bin\mvn.cmd" (
    echo Maven not found at %MVN_HOME%
    exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MAVEN_OPTS=-Xmx512m"
"%MVN_HOME%\bin\mvn.cmd" %*
