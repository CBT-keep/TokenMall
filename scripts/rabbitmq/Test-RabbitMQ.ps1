$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "_RabbitMQEnv.ps1")
Initialize-RabbitMQEnvironment

& $script:ErlExe -version
Write-Host ""
& $script:RabbitMQCtl status
Write-Host ""

$amqpReady = (Test-NetConnection localhost -Port 5672 -WarningAction SilentlyContinue).TcpTestSucceeded
$managementReady = (Test-NetConnection localhost -Port 15672 -WarningAction SilentlyContinue).TcpTestSucceeded

Write-Host "AMQP 5672 ready: $amqpReady"
Write-Host "Management 15672 ready: $managementReady"

if (-not $amqpReady) {
    throw "RabbitMQ AMQP port 5672 is not ready."
}
