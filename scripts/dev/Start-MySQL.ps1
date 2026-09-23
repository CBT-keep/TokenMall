$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "_ServiceHelpers.ps1")

$service = Get-Service -Name MySQL -ErrorAction Stop
if ($service.Status -eq "Running") {
    Write-Host "MySQL is already running."
    exit 0
}

Write-Host "Starting MySQL service..."
Start-Service -Name MySQL -ErrorAction Stop

if (-not (Wait-TcpPort -Port 3306 -TimeoutSeconds 30)) {
    throw "MySQL service started, but port 3306 is not ready."
}

Write-Host "MySQL is running on port 3306."
