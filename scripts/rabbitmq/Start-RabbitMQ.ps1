$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "_RabbitMQEnv.ps1")
Initialize-RabbitMQEnvironment

$existing = Get-Process -Name erl -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "RabbitMQ Erlang process is already running. PID(s): $($existing.Id -join ', ')"
    exit 0
}

$started = Start-Process `
    -FilePath $script:RabbitMQServer `
    -WorkingDirectory (Join-Path $script:RabbitMQHome "sbin") `
    -WindowStyle Hidden `
    -PassThru

Write-Host "RabbitMQ start command launched. Launcher PID: $($started.Id)"
Write-Host "Management UI: http://localhost:15672"
Write-Host "Run Status-RabbitMQ.ps1 to verify readiness."
