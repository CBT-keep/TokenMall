function Wait-TcpPort {
    param(
        [string]$HostName = "localhost",
        [int]$Port,
        [int]$TimeoutSeconds = 30
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $ready = (Test-NetConnection $HostName -Port $Port -WarningAction SilentlyContinue).TcpTestSucceeded
        if ($ready) {
            return $true
        }
        Start-Sleep -Milliseconds 500
    } while ((Get-Date) -lt $deadline)

    return $false
}

function Test-TcpPort {
    param(
        [string]$HostName = "localhost",
        [int]$Port
    )
    return (Test-NetConnection $HostName -Port $Port -WarningAction SilentlyContinue).TcpTestSucceeded
}
