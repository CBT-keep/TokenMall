$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "_ServiceHelpers.ps1")

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$backendRoot = Join-Path $repoRoot "backend"
$stdout = Join-Path $backendRoot "backend-server.log"
$stderr = Join-Path $backendRoot "backend-server-error.log"

$existing = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "Backend already listening on port 8080. PID: $($existing.OwningProcess -join ',')"
    exit 0
}

$mvn = Get-Command mvn -ErrorAction Stop
$process = Start-Process `
    -FilePath $mvn.Source `
    -ArgumentList @("spring-boot:run") `
    -WorkingDirectory $backendRoot `
    -WindowStyle Hidden `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr `
    -PassThru

if (-not (Wait-TcpPort -Port 8080 -TimeoutSeconds 60)) {
    Write-Host "Backend did not become ready. Recent log:"
    if (Test-Path -LiteralPath $stdout) {
        Get-Content -LiteralPath $stdout -Tail 60
    }
    throw "Backend failed to listen on port 8080."
}

Write-Host "Backend started: http://localhost:8080"
Write-Host "Launcher PID: $($process.Id)"
Write-Host "Log: $stdout"
