$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "_RabbitMQEnv.ps1")
Initialize-RabbitMQEnvironment

$vhost = "tokenmall"
$username = "tokenmall_dev"
$password = "ChangeMe_123456"

$vhosts = & $script:RabbitMQCtl list_vhosts
if ($LASTEXITCODE -ne 0) {
    throw "Failed to list RabbitMQ virtual hosts."
}

if ($vhosts -notcontains $vhost) {
    & $script:RabbitMQCtl add_vhost $vhost
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to create virtual host: $vhost"
    }
}

$users = & $script:RabbitMQCtl list_users
if ($LASTEXITCODE -ne 0) {
    throw "Failed to list RabbitMQ users."
}

$userExists = $false
foreach ($line in $users) {
    if ($line -match "^$([regex]::Escape($username))\s") {
        $userExists = $true
        break
    }
}

if (-not $userExists) {
    & $script:RabbitMQCtl add_user $username $password
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to create RabbitMQ user: $username"
    }
}

& $script:RabbitMQCtl set_permissions -p $vhost $username ".*" ".*" ".*"
if ($LASTEXITCODE -ne 0) {
    throw "Failed to grant RabbitMQ permissions."
}

& $script:RabbitMQCtl set_user_tags $username management
if ($LASTEXITCODE -ne 0) {
    throw "Failed to grant RabbitMQ management tag."
}

Write-Host "RabbitMQ project environment initialized."
Write-Host "Virtual host: $vhost"
Write-Host "Username: $username"
Write-Host "Password: $password"
Write-Host "Management UI: http://localhost:15672"
