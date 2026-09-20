$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "_RabbitMQEnv.ps1")
Initialize-RabbitMQEnvironment

& $script:RabbitMQCtl stop
if ($LASTEXITCODE -ne 0) {
    throw "RabbitMQ stop command failed with exit code $LASTEXITCODE."
}

Write-Host "RabbitMQ stop command completed."
