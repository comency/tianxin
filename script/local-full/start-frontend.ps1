# 文件作用：安装依赖并启动 Vue3 管理后台。
# 作者：DAMU
# 创建时间：2026-07-22
# 核心功能：使用 pnpm 启动官方 yudao-ui-admin-vue3 开发服务器。
$ErrorActionPreference = 'Stop'

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$frontendDirectory = Join-Path $repositoryRoot 'yudao-ui\yudao-ui-admin-vue3'

Push-Location $frontendDirectory
try {
    if (-not (Test-Path -LiteralPath 'node_modules')) {
        & pnpm install
        if ($LASTEXITCODE -ne 0) {
            throw '前端依赖安装失败。'
        }
    }

    & pnpm dev
}
finally {
    Pop-Location
}
