$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "_ServiceHelpers.ps1")

$redisRoot = "D:\Redis-x64-3.0.504"
$redisServer = Join-Path $redisRoot "redis-server.exe"
$redisCli = Join-Path $redisRoot "redis-cli.exe"
$redisConfig = Join-Path $redisRoot "redis.windows.conf"
$stdout = Join-Path $redisRoot "redis-server.log"
$stderr = Join-Path $redisRoot "redis-server-error.log"
$pidFile = Join-Path $redisRoot "redis-server.pid"
$password = ""

if (Test-Path -LiteralPath $redisConfig) {
    $passwordLine = Select-String -LiteralPath $redisConfig -Pattern '^\s*requirepass\s+(.+?)\s*$' |
        Select-Object -First 1
    if ($passwordLine) {
        $password = $passwordLine.Matches[0].Groups[1].Value.Trim()
    }
}

if (Test-TcpPort -Port 6379) {
    Write-Host "Redis already listening on port 6379."
    exit 0
}

if (-not (Test-Path -LiteralPath $redisServer)) {
    throw "Redis server not found: $redisServer"
}

$arguments = @()
if (Test-Path -LiteralPath $redisConfig) {
    $arguments += $redisConfig
}

$process = Start-Process `
    -FilePath $redisServer `
    -ArgumentList $arguments `
    -WorkingDirectory $redisRoot `
    -WindowStyle Hidden `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr `
    -PassThru

$process.Id | Set-Content -LiteralPath $pidFile -Encoding ASCII -NoNewline

if (-not (Wait-TcpPort -Port 6379 -TimeoutSeconds 15)) {
    throw "Redis did not start. Check $stderr"
}

$pingArguments = @()
if ($password) {
    $pingArguments += @("-a", $password)
}
$pingArguments += "ping"
$pong = & $redisCli @pingArguments
Write-Host "Redis started: $pong"
Write-Host "Launcher PID: $($process.Id)"
