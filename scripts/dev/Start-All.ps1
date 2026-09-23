param(
    [switch]$NoOpen
)

$ErrorActionPreference = "Continue"

. (Join-Path $PSScriptRoot "_ServiceHelpers.ps1")

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$mysqlService = Get-Service MySQL -ErrorAction SilentlyContinue
$mysqlReady = $false

Write-Host "========================================"
Write-Host " TokenMall one-click startup"
Write-Host "========================================"

if ($mysqlService) {
    if ($mysqlService.Status -ne "Running") {
        Write-Host "[1/4] Starting MySQL..."
        $mysqlHelper = Join-Path $PSScriptRoot "Start-MySQL.ps1"

        try {
            & $mysqlHelper
            $mysqlReady = Test-TcpPort -Port 3306
        }
        catch {
            try {
                Write-Host "Requesting administrator permission to start MySQL..."
                $elevated = Start-Process `
                    -FilePath "powershell.exe" `
                    -Verb RunAs `
                    -Wait `
                    -PassThru `
                    -WindowStyle Hidden `
                    -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-File", $mysqlHelper)

                if ($elevated.ExitCode -ne 0) {
                    throw "Elevated MySQL startup failed with exit code $($elevated.ExitCode)."
                }

                $mysqlReady = Test-TcpPort -Port 3306
            }
            catch {
                Write-Warning "MySQL could not be started automatically: $($_.Exception.Message)"
            }
        }

        if (-not $mysqlReady) {
            Write-Warning "MySQL is not ready. The backend API will fail until it is running."
        }
    }
    else {
        Write-Host "[1/4] MySQL already running."
        $mysqlReady = $true
    }
}
else {
    Write-Warning "MySQL Windows service was not found."
}

Write-Host "[2/4] Starting RabbitMQ..."
& (Join-Path $PSScriptRoot "..\rabbitmq\Start-RabbitMQ.ps1")
$rabbitReady = Wait-TcpPort -Port 5672 -TimeoutSeconds 30
if (-not $rabbitReady) {
    Write-Warning "RabbitMQ is not ready. Check scripts\rabbitmq\Status-RabbitMQ.ps1."
}

Write-Host "[3/4] Starting Redis..."
& (Join-Path $PSScriptRoot "Start-Redis.ps1")
$redisReady = Test-TcpPort -Port 6379

Write-Host "[4/4] Starting frontend..."
& (Join-Path $PSScriptRoot "Start-Frontend.ps1")
$frontendReady = Test-TcpPort -Port 5173

Write-Host ""
$allReady = $mysqlReady -and $rabbitReady -and $redisReady -and $frontendReady
if ($allReady) {
    Write-Host "TokenMall is ready."
}
else {
    Write-Warning "TokenMall startup is incomplete."
}
Write-Host "Frontend:  http://127.0.0.1:5173"
Write-Host "RabbitMQ:  http://localhost:15672"
Write-Host "Login:     admin / admin123"

if (Test-TcpPort -Port 8080) {
    Write-Host "Backend:   already running on http://localhost:8080"
}
else {
    Write-Host ""
    Write-Host "Backend was not started by this script."
    Write-Host "Start TokenMallApplication in IntelliJ IDEA, then open:"
    Write-Host "http://localhost:8080"
}

if (-not $NoOpen) {
    if (Test-TcpPort -Port 5173) {
        Start-Process "http://127.0.0.1:5173"
    }
    else {
        Write-Warning "Frontend is not ready. Open http://127.0.0.1:5173 manually after it starts."
    }
}

if (-not $allReady) {
    exit 1
}
