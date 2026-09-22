param(
    [switch]$NoOpen
)

$ErrorActionPreference = "Continue"

. (Join-Path $PSScriptRoot "_ServiceHelpers.ps1")

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$mysqlService = Get-Service MySQL -ErrorAction SilentlyContinue

Write-Host "========================================"
Write-Host " TokenMall one-click startup"
Write-Host "========================================"

if ($mysqlService) {
    if ($mysqlService.Status -ne "Running") {
        Write-Host "[1/5] Starting MySQL..."
        try {
            Start-Service MySQL -ErrorAction Stop
            if (-not (Wait-TcpPort -Port 3306 -TimeoutSeconds 30)) {
                throw "MySQL port 3306 is not ready."
            }
        }
        catch {
            Write-Warning "MySQL could not be started automatically: $($_.Exception.Message)"
            Write-Warning "Open Services.msc and start MySQL as administrator, then run this script again."
        }
    }
    else {
        Write-Host "[1/5] MySQL already running."
    }
}
else {
    Write-Warning "MySQL Windows service was not found."
}

Write-Host "[2/5] Starting RabbitMQ..."
& (Join-Path $PSScriptRoot "..\rabbitmq\Start-RabbitMQ.ps1")
if (-not (Wait-TcpPort -Port 5672 -TimeoutSeconds 30)) {
    Write-Warning "RabbitMQ is not ready. Check scripts\rabbitmq\Status-RabbitMQ.ps1."
}

Write-Host "[3/5] Starting Redis..."
& (Join-Path $PSScriptRoot "Start-Redis.ps1")

Write-Host "[4/5] Starting backend..."
& (Join-Path $PSScriptRoot "Start-Backend.ps1")

Write-Host "[5/5] Starting frontend..."
& (Join-Path $PSScriptRoot "Start-Frontend.ps1")

Write-Host ""
Write-Host "TokenMall is ready."
Write-Host "Frontend:  http://127.0.0.1:5173"
Write-Host "Backend:   http://localhost:8080"
Write-Host "RabbitMQ:  http://localhost:15672"
Write-Host "Login:     admin / admin123"

if (-not $NoOpen) {
    Start-Process "http://127.0.0.1:5173"
}
