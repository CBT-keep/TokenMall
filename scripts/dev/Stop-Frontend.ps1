$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$frontendRoot = Join-Path $repoRoot "frontend"
$pidFile = Join-Path $frontendRoot ".frontend.pid"
$port = 5173

$listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
if ($listeners) {
    foreach ($processId in ($listeners.OwningProcess | Select-Object -Unique)) {
        Stop-Process -Id $processId -ErrorAction SilentlyContinue
        Write-Host "Stopped frontend listener process $processId."
    }
}
else {
    Write-Host "Frontend is not listening on port $port."
}

if (Test-Path -LiteralPath $pidFile) {
    Remove-Item -LiteralPath $pidFile -Force
}
