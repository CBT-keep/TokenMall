$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "_RabbitMQEnv.ps1")
Initialize-RabbitMQEnvironment

function Stop-EpmdIfUnused {
    $erlProcesses = @(Get-Process -Name erl -ErrorAction SilentlyContinue)
    if ($erlProcesses.Count -gt 0) {
        return
    }

    $epmdProcesses = @(Get-Process -Name epmd -ErrorAction SilentlyContinue)
    if ($epmdProcesses.Count -eq 0) {
        return
    }

    $epmdProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "Stopped RabbitMQ epmd helper process."
}

$erlProcesses = @(Get-Process -Name erl -ErrorAction SilentlyContinue)
$listeners = Get-NetTCPConnection -LocalPort 5672 -State Listen -ErrorAction SilentlyContinue

if ($erlProcesses.Count -eq 0 -and -not $listeners) {
    Write-Host "RabbitMQ is not running."
    Stop-EpmdIfUnused
    exit 0
}

& $script:RabbitMQCtl stop
if ($LASTEXITCODE -ne 0) {
    $remainingErl = @(Get-Process -Name erl -ErrorAction SilentlyContinue)
    $remainingListener = Get-NetTCPConnection -LocalPort 5672 -State Listen -ErrorAction SilentlyContinue
    if ($remainingErl.Count -eq 0 -and -not $remainingListener) {
        Write-Host "RabbitMQ broker was already stopped."
        Stop-EpmdIfUnused
        exit 0
    }

    throw "RabbitMQ stop command failed with exit code $LASTEXITCODE."
}

Write-Host "RabbitMQ stop command completed."
Stop-EpmdIfUnused
