$ErrorActionPreference = "Stop"

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

Write-Host "Backend launcher PID: $($process.Id)"
Write-Host "Log: $stdout"
