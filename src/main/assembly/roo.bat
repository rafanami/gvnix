@echo off
setlocal enabledelayedexpansion

for %%? in ("%~dp0..") do set ROO_HOME=%%~f?
rem echo Resolved ROO_HOME: "%ROO_HOME%"

rem Build a classpath containing our two magical startup JARs
for %%a in ("%ROO_HOME%\bin\*.jar") do set ROO_CP=!ROO_CP!%%a;

rem Hop, hop, hop...
java -Droo.home="%ROO_HOME%" -Droo.args="%*" -DdevelopmentMode=false -Dorg.osgi.framework.storage="%ROO_HOME%\cache" -Dfelix.auto.deploy.dir="%ROO_HOME%\bundle" -Dfelix.config.properties="file:%ROO_HOME%\conf\config.properties" -cp "%ROO_CP%" org.springframework.roo.bootstrap.Main
rem echo Roo exited with code %errorlevel%

:end
