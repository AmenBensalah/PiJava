@echo off
REM ----------------------------------------------------------------------------
REM Maven Wrapper
REM ----------------------------------------------------------------------------
setlocal
set MAVEN_PROJECTBASEDIR=%~dp0
set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar
if exist "%JAVA_HOME%\bin\java.exe" (
  set JAVACMD=%JAVA_HOME%\bin\java.exe
) else (
  set JAVACMD=java
)
if not exist "%WRAPPER_JAR%" (
  echo Maven Wrapper jar not found. Please run the wrapper generation or install Maven.
  exit /b 1
)
"%JAVACMD%" -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR% -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
