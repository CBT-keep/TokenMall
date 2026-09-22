$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$desktop = [Environment]::GetFolderPath("Desktop")
$shell = New-Object -ComObject WScript.Shell

$startShortcut = $shell.CreateShortcut((Join-Path $desktop "TokenMall 一键启动.lnk"))
$startShortcut.TargetPath = "$env:SystemRoot\System32\cmd.exe"
$startShortcut.Arguments = "/c `"$repoRoot\启动TokenMall.cmd`""
$startShortcut.WorkingDirectory = $repoRoot
$startShortcut.IconLocation = "$env:SystemRoot\System32\shell32.dll,137"
$startShortcut.Description = "Start TokenMall development environment"
$startShortcut.Save()

$stopShortcut = $shell.CreateShortcut((Join-Path $desktop "TokenMall 一键停止.lnk"))
$stopShortcut.TargetPath = "$env:SystemRoot\System32\cmd.exe"
$stopShortcut.Arguments = "/c `"$repoRoot\停止TokenMall.cmd`""
$stopShortcut.WorkingDirectory = $repoRoot
$stopShortcut.IconLocation = "$env:SystemRoot\System32\shell32.dll,131"
$stopShortcut.Description = "Stop TokenMall development environment"
$stopShortcut.Save()

Write-Host "Desktop shortcuts created:"
Write-Host " - TokenMall 一键启动"
Write-Host " - TokenMall 一键停止"
