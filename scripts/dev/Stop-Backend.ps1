$ErrorActionPreference = "Stop"

$port = 8080
$listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue

if (-not $listeners) {
    Write-Host "Backend is not listening on port $port."
    exit 0
}

foreach ($processId in ($listeners.OwningProcess | Select-Object -Unique)) {
    Stop-Process -Id $processId -ErrorAction SilentlyContinue
    Write-Host "Stopped backend listener process $processId."
}
