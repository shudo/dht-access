@echo off

if "%DHTACCESS_HOME%" == "" set DHTACCESS_HOME=%~dp0..
set BIN_DIR=%DHTACCESS_HOME%\bin
set LIB_DIR=%DHTACCESS_HOME%\lib
set TARGET_DIR=%DHTACCESS_HOME%\target
set BUILD_DIR=%DHTACCESS_HOME%\build

set CLASSPATH=%BUILD_DIR%;%TARGET_DIR%\dhtaccess.jar;%LIB_DIR%\xmlrpc-common-3.1.3.jar;%LIB_DIR%\xmlrpc-client-3.1.3.jar;%LIB_DIR%\ws-commons-util-1.0.2.jar;%LIB_DIR%\commons-cli-1.2.jar
set LOGGING_CONFIG=%BIN_DIR%\logging.properties

set JVM_OPTION=-Djava.util.logging.config.file=%LOGGING_CONFIG% -Ddhtaccess.gateway=%DHT_GATEWAY%

java %JVM_OPTION% dhtaccess.tools.Get %*
