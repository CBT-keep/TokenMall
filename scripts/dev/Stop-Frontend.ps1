$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$frontendRoot = Join-Path $repoRoot "frontend"
$pidFile = Join-Path $frontendRoot ".frontend.pid"

if (Test-Path -LiteralPath $pidFile) {
    $processId = [int](Get-Content -LiteralPath $pidFile -Raw)
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($process) {
        Stop-Process -Id $processId
        Write-Host "Stopped frontend process $processId."
    }
    Remove-Item -LiteralPath $pidFile -Force
}
else {
    $existing = Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue
    if ($existing) {
        throw "Frontend is listening on port 5173 but no PID file exists. Stop PID $($existing.OwningProcess -join ',') manually."
    }
    Write-Host "Frontend is not running."
}
