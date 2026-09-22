@echo off
chcp 65001 >nul
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\dev\Start-All.ps1"
echo.
echo 如果窗口中有错误，请截图发给 Codex。
pause
