$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "_ServiceHelpers.ps1")

$redisRoot = "D:\Redis-x64-3.0.504"
$redisCli = Join-Path $redisRoot "redis-cli.exe"
$pidFile = Join-Path $redisRoot "redis-server.pid"
$redisConfig = Join-Path $redisRoot "redis.windows.conf"
$password = ""

if (Test-Path -LiteralPath $redisConfig) {
    $passwordLine = Select-String -LiteralPath $redisConfig -Pattern '^\s*requirepass\s+(.+?)\s*$' |
        Select-Object -First 1
    if ($passwordLine) {
        $password = $passwordLine.Matches[0].Groups[1].Value.Trim()
    }
}

if (-not (Test-TcpPort -Port 6379)) {
    Write-Host "Redis is not running."
    exit 0
}

if (Test-Path -LiteralPath $redisCli) {
    $shutdownArguments = @()
    if ($password) {
        $shutdownArguments += @("-a", $password)
    }
    $shutdownArguments += @("shutdown", "nosave")
    & $redisCli @shutdownArguments | Out-Null
    Start-Sleep -Seconds 1
}

if (Test-TcpPort -Port 6379) {
    $listeners = Get-NetTCPConnection -LocalPort 6379 -State Listen -ErrorAction SilentlyContinue
    foreach ($processId in ($listeners.OwningProcess | Select-Object -Unique)) {
        Stop-Process -Id $processId -ErrorAction SilentlyContinue
    }
}

if (Test-Path -LiteralPath $pidFile) {
    Remove-Item -LiteralPath $pidFile -Force
}

Write-Host "Redis stopped."
