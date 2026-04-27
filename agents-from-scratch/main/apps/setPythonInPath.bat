@echo off
SET "TARGET_PATH=%USERPROFILE%\AppData\Local\Packages\PythonSoftwareFoundation.Python.3.13_qbz5n2kfra8p0\LocalCache\local-packages\Python313\Scripts"

REM Check if the path is already in the user PATH
echo %PATH% | find /I "%TARGET_PATH%" >nul
IF %ERRORLEVEL%==0 (
    echo 🔄 PATH already contains the Python Scripts directory.
) ELSE (
    echo ➕ Adding Python Scripts directory to user PATH...
    setx PATH "%PATH%;%TARGET_PATH%"
    echo ✅ PATH updated. Please restart your terminal to apply changes.
)
pause
