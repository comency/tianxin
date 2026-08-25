# 文件作用：以 JDK 25 构建并启动全模块后端。
# 作者：DAMU
# 创建时间：2026-07-22
# 核心功能：固定 Java 运行时，选择 all-local profile 启动 yudao-server。
param(
    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'
if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $javaCommand = Get-Command java -ErrorAction Stop
    $env:JAVA_HOME = Split-Path (Split-Path $javaCommand.Source -Parent) -Parent
}
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

if ([string]::IsNullOrWhiteSpace($env:TX_DB_PASSWORD)) {
    throw '请先设置 TX_DB_PASSWORD 环境变量，避免把 MySQL 密码写进配置文件。'
}

Set-Location $repositoryRoot
java -version

$mavenArguments = @(
    '-Dmaven.compiler.source=25',
    '-Dmaven.compiler.target=25',
    '-Dmaven.compiler.release=25',
    '-pl',
    'yudao-server',
    '-am',
    'package'
)
if ($SkipTests) {
    $mavenArguments += '-Dmaven.test.skip=true'
}

& mvn @mavenArguments
if ($LASTEXITCODE -ne 0) {
    throw '后端打包失败，请查看 Maven 输出定位根因。'
}

$serverJar = Join-Path $repositoryRoot 'yudao-server\target\yudao-server.jar'
if (-not (Test-Path -LiteralPath $serverJar)) {
    throw "找不到后端可执行 Jar：$serverJar"
}

& java -jar $serverJar '--spring.profiles.active=all-local'
if ($LASTEXITCODE -ne 0) {
    throw '后端启动失败，请查看应用日志定位根因。'
}
