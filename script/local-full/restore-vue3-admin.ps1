# 文件作用：恢复完整的官方 Vue3 管理后台工程。
# 作者：DAMU
# 创建时间：2026-07-22
# 核心功能：备份本地 MES 覆盖文件，拉取官方基线，再恢复本地覆盖层。
$ErrorActionPreference = 'Stop'

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$targetDirectory = Join-Path $repositoryRoot 'yudao-ui\yudao-ui-admin-vue3'
$backupDirectory = Join-Path $PSScriptRoot ("backup\vue3-overlay-" + (Get-Date -Format 'yyyyMMdd-HHmmss'))
$sourceDirectory = Join-Path $PSScriptRoot 'work\yudao-ui-admin-vue3'

if (Test-Path -LiteralPath $sourceDirectory) {
    throw "官方 Vue3 基线临时目录已存在：$sourceDirectory。请先检查并手动处理后重试。"
}

New-Item -ItemType Directory -Force $backupDirectory, (Split-Path $sourceDirectory) | Out-Null
Copy-Item -LiteralPath (Join-Path $targetDirectory 'src') -Destination $backupDirectory -Recurse -Force
git clone --depth 1 https://gitee.com/yudaocode/yudao-ui-admin-vue3.git $sourceDirectory
if ($LASTEXITCODE -ne 0) {
    throw '拉取官方 yudao-ui-admin-vue3 基线失败。'
}

Copy-Item -Path (Join-Path $sourceDirectory '*') -Destination $targetDirectory -Recurse -Force
Copy-Item -LiteralPath (Join-Path $backupDirectory 'src\api\mes') -Destination (Join-Path $targetDirectory 'src\api') -Recurse -Force
Copy-Item -LiteralPath (Join-Path $backupDirectory 'src\views\mes') -Destination (Join-Path $targetDirectory 'src\views') -Recurse -Force

Write-Host '官方 Vue3 基线已恢复，本地 MES 覆盖文件已合并。'
