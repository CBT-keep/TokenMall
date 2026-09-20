$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$frontendRoot = Join-Path $repoRoot "frontend"
$port = 5173
$pidFile = Join-Path $frontendRoot ".frontend.pid"
$stdout = Join-Path $frontendRoot "dev-server.log"
$stderr = Join-Path $frontendRoot "dev-server-error.log"

$existing = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "Frontend already listening on port $port. PID: $($existing.OwningProcess -join ',')"
    exit 0
}

$pnpmCmd = Join-Path $env:APPDATA "npm\pnpm.cmd"
if (-not (Test-Path -LiteralPath $pnpmCmd)) {
    throw "pnpm.cmd not found at $pnpmCmd"
}

$process = Start-Process `
    -FilePath $pnpmCmd `
    -ArgumentList @("dev", "--host", "127.0.0.1") `
    -WorkingDirectory $frontendRoot `
    -WindowStyle Hidden `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr `
    -PassThru

$process.Id | Set-Content -LiteralPath $pidFile -Encoding ASCII -NoNewline
Start-Sleep -Seconds 4

$response = Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:$port" -TimeoutSec 10
Write-Host "Frontend started: http://127.0.0.1:$port"
Write-Host "Launcher PID: $($process.Id)"
Write-Host "HTTP status: $($response.StatusCode)"
