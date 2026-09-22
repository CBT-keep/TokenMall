$ErrorActionPreference = "Continue"

Write-Host "Stopping TokenMall development services..."

& (Join-Path $PSScriptRoot "Stop-Frontend.ps1")
& (Join-Path $PSScriptRoot "Stop-Backend.ps1")
& (Join-Path $PSScriptRoot "Stop-Redis.ps1")
& (Join-Path $PSScriptRoot "..\rabbitmq\Stop-RabbitMQ.ps1")

Write-Host ""
Write-Host "Application services stopped."
Write-Host "MySQL is a Windows service and was left running."
