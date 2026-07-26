@echo off
setlocal
REM Prepare analyzer-libs from a Maven project (pass project dir as %1, default=.)
set PROJ=%~1
if "%PROJ%"=="" set PROJ=.
set OUT=%PROJ%\analyzer-libs
echo Copying Maven dependencies to %OUT%
mvn -f "%PROJ%\pom.xml" -q dependency:copy-dependencies -DoutputDirectory="%OUT%"
if errorlevel 1 (
  echo FAILED: run from a Maven project or pass path to pom directory
  exit /b 1
)
echo Done. Use: --lib-dir "%OUT%"
endlocal
