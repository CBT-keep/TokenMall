$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "_RabbitMQEnv.ps1")
Initialize-RabbitMQEnvironment

Write-Host "Erlang home: $script:ErlangHome"
Write-Host "RabbitMQ home: $script:RabbitMQHome"
Write-Host "RabbitMQ data: $script:RabbitMQData"
Write-Host "RabbitMQ logs: $script:RabbitMQLogs"
Write-Host ""

& $script:RabbitMQCtl status
$statusExitCode = $LASTEXITCODE

Write-Host ""
Write-Host "Port 5672: $((Test-NetConnection localhost -Port 5672 -WarningAction SilentlyContinue).TcpTestSucceeded)"
Write-Host "Port 15672: $((Test-NetConnection localhost -Port 15672 -WarningAction SilentlyContinue).TcpTestSucceeded)"

if ($statusExitCode -ne 0) {
    exit $statusExitCode
}
