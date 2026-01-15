@echo off
REM ==== Copy this file to run-jar.bat ====
REM ==== Fill real values below ====

REM ==== Environment Variables ====
set DB_URL=db_url
set DB_USERNAME=your_username
set DB_PASSWORD=your_password

REM === Run Application ====
java -jar build\libs\metasns-api-v1.jar