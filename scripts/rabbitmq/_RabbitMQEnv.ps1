$script:RabbitMQBase = "D:\DevTools\RabbitMQ"
$script:RabbitMQData = Join-Path $script:RabbitMQBase "data"
$script:RabbitMQLogs = Join-Path $script:RabbitMQBase "logs"
$script:ErlangHome = Join-Path $script:RabbitMQBase "erlang-28.5"
$script:RabbitMQHome = Join-Path $script:RabbitMQBase "rabbitmq-server-4.3.6"

if (Test-Path -LiteralPath (Join-Path $script:ErlangHome "otp_win64_28.5\bin\erl.exe")) {
    $script:ErlangHome = Join-Path $script:ErlangHome "otp_win64_28.5"
}

if (Test-Path -LiteralPath (Join-Path $script:RabbitMQHome "rabbitmq_server-4.3.6\sbin\rabbitmq-server.bat")) {
    $script:RabbitMQHome = Join-Path $script:RabbitMQHome "rabbitmq_server-4.3.6"
}

$script:ErlExe = Join-Path $script:ErlangHome "bin\erl.exe"
$script:RabbitMQCtl = Join-Path $script:RabbitMQHome "sbin\rabbitmqctl.bat"
$script:RabbitMQPlugins = Join-Path $script:RabbitMQHome "sbin\rabbitmq-plugins.bat"
$script:RabbitMQServer = Join-Path $script:RabbitMQHome "sbin\rabbitmq-server.bat"

function Initialize-RabbitMQEnvironment {
    if (-not (Test-Path -LiteralPath $script:ErlExe)) {
        throw "Erlang executable not found: $script:ErlExe"
    }
    if (-not (Test-Path -LiteralPath $script:RabbitMQServer)) {
        throw "RabbitMQ server script not found: $script:RabbitMQServer"
    }

    New-Item -ItemType Directory -Path $script:RabbitMQData -Force | Out-Null
    New-Item -ItemType Directory -Path $script:RabbitMQLogs -Force | Out-Null

    $env:ERLANG_HOME = $script:ErlangHome
    $env:RABBITMQ_BASE = $script:RabbitMQData
    $env:RABBITMQ_LOG_BASE = $script:RabbitMQLogs
    $env:RABBITMQ_MNESIA_BASE = Join-Path $script:RabbitMQData "mnesia"
    $env:RABBITMQ_ENABLED_PLUGINS_FILE = Join-Path $script:RabbitMQData "enabled_plugins"
    $env:HOME = $script:RabbitMQData
    $env:HOMEDRIVE = "D:"
    $env:HOMEPATH = "\DevTools\RabbitMQ\data"
    $env:PATH = "$($script:ErlangHome)\bin;$($script:RabbitMQHome)\sbin;$env:PATH"

    New-Item -ItemType Directory -Path $env:RABBITMQ_MNESIA_BASE -Force | Out-Null
}
