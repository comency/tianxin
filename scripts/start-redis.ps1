$ErrorActionPreference = 'Stop'

$redisRoot = Join-Path $PSScriptRoot '..\.local\redis'
$dataRoot = Join-Path $PSScriptRoot '..\.local\redis-data'
$serviceWrapper = Join-Path $redisRoot 'RedisService.exe'
$cli = Join-Path $redisRoot 'redis-cli.exe'

New-Item -ItemType Directory -Force -Path $dataRoot | Out-Null

try {
    $pong = & $cli -p 6379 PING 2>$null
    if ($pong -eq 'PONG') {
        Write-Output 'Redis is already running on 127.0.0.1:6379.'
        exit 0
    }
} catch {
    # Start Redis below when the local endpoint is unavailable.
}

Start-Process -FilePath $serviceWrapper `
    -ArgumentList @('run', '--port', '6379', '--dir', $dataRoot) `
    -WorkingDirectory $redisRoot `
    -WindowStyle Hidden

for ($attempt = 0; $attempt -lt 20; $attempt++) {
    Start-Sleep -Milliseconds 250
    $pong = & $cli -p 6379 PING 2>$null
    if ($pong -eq 'PONG') {
        Write-Output 'Redis started on 127.0.0.1:6379.'
        exit 0
    }
}

throw 'Redis did not become ready on port 6379 within 5 seconds.'
