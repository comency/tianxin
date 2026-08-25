$ErrorActionPreference = 'Stop'

$cli = Join-Path $PSScriptRoot '..\.local\redis\redis-cli.exe'
& $cli -p 6379 SHUTDOWN
Write-Output 'Redis stopped.'
