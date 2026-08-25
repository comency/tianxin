# 文件作用：初始化全模块本地 MySQL 数据库。
# 作者：DAMU
# 创建时间：2026-07-22
# 核心功能：备份已有数据库、重建 UTF-8 数据库，并导入基础与业务模块 SQL 脚本。
param(
    [switch]$ConfirmReset,
    [string]$DatabasePassword = $env:TX_DB_PASSWORD
)

$ErrorActionPreference = 'Stop'

$databaseName = 'ruoyi-vue-pro'
$databaseUser = 'root'
if ([string]::IsNullOrWhiteSpace($DatabasePassword)) {
    throw '请设置 TX_DB_PASSWORD 环境变量，或通过 -DatabasePassword 参数提供本机 MySQL 密码。'
}
$mysqlArguments = @("-u$databaseUser", "-p$DatabasePassword")
$scriptRoot = $PSScriptRoot
$repositoryRoot = (Resolve-Path (Join-Path $scriptRoot '..\..')).Path
$sqlDirectory = Join-Path $repositoryRoot 'sql\mysql'
$backupDirectory = Join-Path $scriptRoot 'backup'
$workDirectory = Join-Path $scriptRoot 'work\sql'
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'

if (-not $ConfirmReset) {
    throw '数据库重建需要显式传入 -ConfirmReset。'
}

function Invoke-MySqlFile {
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath
    )

    if (-not (Test-Path -LiteralPath $FilePath)) {
        throw "找不到 SQL 文件：$FilePath"
    }

    Get-Content -Raw -Encoding utf8 -LiteralPath $FilePath |
        & mysql @mysqlArguments '--default-character-set=utf8mb4' '--binary-mode=1' $databaseName
    if ($LASTEXITCODE -ne 0) {
        throw "导入 SQL 文件失败：$FilePath"
    }
}

function Invoke-MySqlQuery {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Query,

        [string]$DatabaseName
    )

    $queryArguments = @($mysqlArguments + '-N')
    if ($DatabaseName) {
        $queryArguments += $DatabaseName
    }
    $queryArguments += @('-e', $Query)

    & mysql @queryArguments
    if ($LASTEXITCODE -ne 0) {
        throw "执行 MySQL 查询失败：$Query"
    }
}

# 为已有数据库创建可恢复的完整备份；首次初始化时数据库不存在，跳过备份。
New-Item -ItemType Directory -Force $backupDirectory, $workDirectory | Out-Null
$databaseExists = Invoke-MySqlQuery "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '$databaseName';"
if ($databaseExists -eq $databaseName) {
    $backupPath = Join-Path $backupDirectory "$databaseName-$timestamp.sql"
    & mysqldump @mysqlArguments '--databases' $databaseName |
        Set-Content -Encoding utf8 -LiteralPath $backupPath
    if ($LASTEXITCODE -ne 0) {
        throw "备份数据库失败：$backupPath"
    }
}

Invoke-MySqlQuery "DROP DATABASE IF EXISTS ``$databaseName``;"
Invoke-MySqlQuery "CREATE DATABASE ``$databaseName`` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

Invoke-MySqlFile (Join-Path $sqlDirectory 'ruoyi-vue-pro.sql')
Invoke-MySqlFile (Join-Path $sqlDirectory 'quartz.sql')

$moduleSqlArchives = [ordered]@{
    ai = 'ai-2026-04-16-传播违法.sql.zip'
    bpm = 'bpm-2026-04-18-传播违法.sql.zip'
    crm = 'crm-2026-04-18-传播违法.sql.zip'
    erp = 'erp-2026-04-18-传播违法.sql.zip'
    im = 'im-2026-06-20-传播违法.sql.zip'
    iot = 'iot-2026-06-20-传播违法.sql.zip'
    mall = 'mall-2026-04-18-传播违法.sql.zip'
    member = 'member-2026-05-30-传播违法.sql.zip'
    mes = 'mes-2026-06-20-传播违法.sql.zip'
    mp = 'mp-2026-04-18-传播违法.sql.zip'
    pay = 'pay-2026-04-18-传播违法.sql.zip'
    wms = 'wms-2026-05-15-传播违法.sql.zip'
}

foreach ($moduleName in $moduleSqlArchives.Keys) {
    $archivePath = Join-Path $sqlDirectory $moduleSqlArchives[$moduleName]
    $moduleWorkDirectory = Join-Path $workDirectory $moduleName

    if (-not (Test-Path -LiteralPath $archivePath)) {
        throw "找不到 $moduleName 模块 SQL 压缩包：$archivePath"
    }

    New-Item -ItemType Directory -Force $moduleWorkDirectory | Out-Null
    Expand-Archive -LiteralPath $archivePath -DestinationPath $moduleWorkDirectory -Force
    $sqlFiles = Get-ChildItem -LiteralPath $moduleWorkDirectory -Filter '*.sql' -File -Recurse |
        Where-Object { $_.FullName -notmatch '__MACOSX' }

    if ($sqlFiles.Count -ne 1) {
        throw "$moduleName 模块压缩包应只包含一个可导入 SQL 文件，实际数量：$($sqlFiles.Count)"
    }

    Invoke-MySqlFile $sqlFiles[0].FullName
}

Invoke-MySqlFile (Join-Path $sqlDirectory 'tianxin-enterprise-directory.sql')

Invoke-MySqlQuery "SELECT COUNT(*) AS menu_count FROM system_menu;" -DatabaseName $databaseName
Invoke-MySqlQuery "SELECT category, COUNT(*) AS enterprise_count FROM tianxin_enterprise WHERE deleted = b'0' GROUP BY category ORDER BY category;" -DatabaseName $databaseName
Write-Host "数据库 $databaseName 已初始化完成。"
