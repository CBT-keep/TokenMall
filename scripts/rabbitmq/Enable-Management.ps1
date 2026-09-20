$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "_RabbitMQEnv.ps1")
Initialize-RabbitMQEnvironment

& $script:RabbitMQPlugins enable rabbitmq_management --offline
if ($LASTEXITCODE -ne 0) {
    throw "Failed to enable rabbitmq_management."
}

Write-Host "rabbitmq_management enabled."
Write-Host "Restart RabbitMQ if the management UI is not already available."
