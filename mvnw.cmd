@ECHO OFF
SETLOCAL

SET "MAVEN_PROJECTBASEDIR=%~dp0"
IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

SET "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
SET "PROPERTIES_FILE=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

IF NOT EXIST "%PROPERTIES_FILE%" (
  ECHO Missing %PROPERTIES_FILE%
  EXIT /B 1
)

FOR /F "tokens=1,* delims==" %%A IN ('findstr /R /C:"^wrapperUrl=" "%PROPERTIES_FILE%"') DO SET "WRAPPER_URL=%%B"

IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Downloading Maven Wrapper...
  IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" mkdir "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$u='%WRAPPER_URL%'; $p='%WRAPPER_JAR%';" ^
    "Invoke-WebRequest -UseBasicParsing -Uri $u -OutFile $p"
  IF ERRORLEVEL 1 EXIT /B 1
)

IF NOT DEFINED JAVA_HOME (
  SET "JAVA_EXE=java"
) ELSE (
  SET "JAVA_EXE=%JAVA_HOME%\bin\java"
)

"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
ENDLOCAL
