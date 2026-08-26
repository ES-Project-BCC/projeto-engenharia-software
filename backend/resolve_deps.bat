@echo off
set "JAVA_HOME=C:\Users\BOLSONARO2022\Desktop\jdk-26.0.2.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call mvnw.cmd dependency:resolve
