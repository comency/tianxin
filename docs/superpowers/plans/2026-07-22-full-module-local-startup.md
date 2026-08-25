# Full Module Local Startup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development`
> (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox
> (`- [ ]`) syntax for tracking.

**Goal:** 在本机以 JDK 25 启动芋道源码的全部业务模块，并使用完整 Vue3 管理后台访问所有已启用菜单。

**Architecture:** 根 Maven Reactor 与 `yudao-server` 同步启用全部业务模块；新增独立的本地全模块
profile，保留 MySQL、Redis 与本地页面访问，排除未配置外部服务的启动期自动装配。数据库从基础基线重建，
每个业务模块仅导入一个与当前源码匹配的最新增量包。前端从官方 Vue3 工程恢复完整基线，再合并现有 MES
覆盖文件。

**Tech Stack:** JDK 25.0.2、Spring Boot 4.1、Maven 3.9.15、MySQL 8.4、Redis、Node.js 22、
Vue 3、Element Plus、Vite。

## Global Constraints

- 所有新建和修改文件使用 UTF-8；PowerShell 文件保存为 UTF-8，Java 与 YAML 使用 4 空格缩进。
- 每个启动命令在当前进程中设置 `JAVA_HOME=D:\jdk-25.0.2`，并将 `%JAVA_HOME%\bin` 放在 `Path` 首位。
- 使用 MySQL 用户 `root`、密码 `root`、数据库名 `ruoyi-vue-pro`；重建前必须生成带时间戳的备份。
- 启用系统、基础设施、会员、BPM、报表、公众号、支付、商城、CRM、ERP、AI、IoT、MES、WMS、IM。
- 当前阶段不配置真实 AI、微信、支付、IoT、IM 或消息中间件凭据；缺少外部依赖不得阻塞应用启动。
- 前端基线必须是官方 `yudao-ui-admin-vue3`，不可使用 Vue2 的 RuoYi-Vue `ruoyi-ui` 作为运行工程。
- 当前工作目录无 Git 元数据；任务中的提交步骤改为记录 `git status` 不可用并保留变更清单。

---

## File Structure

- Create: `script/local-full/initialize-database.ps1`：备份、重建和按明确列表导入 MySQL 脚本。
- Create: `script/local-full/start-backend.ps1`：强制使用 JDK 25 构建和启动后端。
- Create: `script/local-full/restore-vue3-admin.ps1`：获取官方 Vue3 基线并安全合并本地 MES 覆盖文件。
- Create: `script/local-full/start-frontend.ps1`：安装前端依赖并启动开发服务器。
- Create: `yudao-server/src/main/resources/application-all-local.yaml`：全模块本地 profile。
- Modify: `pom.xml`：启用全部根 Maven 模块。
- Modify: `yudao-server/pom.xml`：启用全部服务器业务依赖。
- Modify: `yudao-ui/yudao-ui-admin-vue3/`：由官方完整 Vue3 工程补齐构建、路由、组件和依赖文件，保留 MES
  覆盖文件。

## Task 1: 固化本地环境与前置连通性

**Files:**

- Create: `script/local-full/start-backend.ps1`
- Test: PowerShell、Java、Maven、MySQL、Redis 命令输出

**Interfaces:**

- Consumes: `D:\jdk-25.0.2`、`mysql.exe`、`redis-cli.exe` 与 Maven 命令。
- Produces: 可复用的后端启动脚本和前置检查结果。

- [ ] **Step 1: 编写失败预检命令**

```powershell
$env:JAVA_HOME = 'D:\jdk-25.0.2'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
mvn -version
mysql -uroot -proot -e 'SELECT VERSION();'
redis-cli ping
```

Expected: 当前旧会话会显示 Java 17 或找不到 `redis-cli`，证明不能依赖旧环境继承。

- [ ] **Step 2: 运行失败预检并记录解析路径**

Run: 上述 PowerShell 命令。

Expected: `java -version` 显示 `25.0.2`；MySQL 查询返回版本；Redis 返回 `PONG`。

- [ ] **Step 3: 写入最小后端启动脚本**

```powershell
param(
    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = 'D:\jdk-25.0.2'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$mavenArguments = @('-pl', 'yudao-server', '-am', 'spring-boot:run', '-Dspring-boot.run.profiles=all-local')
if ($SkipTests) {
    $mavenArguments += '-DskipTests'
}

java -version
mvn @mavenArguments
```

- [ ] **Step 4: 验证脚本语法**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File script/local-full/start-backend.ps1 -?`

Expected: PowerShell 输出参数帮助且不出现解析错误。

- [ ] **Step 5: 记录变更清单**

Run: `Get-ChildItem script/local-full | Select-Object Name, Length`

Expected: 显示 `start-backend.ps1`；不执行 Git 提交，因为工作目录没有 `.git`。

## Task 2: 备份并重建全模块数据库

**Files:**

- Create: `script/local-full/initialize-database.ps1`
- Read: `sql/mysql/ruoyi-vue-pro.sql`、`sql/mysql/quartz.sql` 和各模块的最新 SQL 压缩包
- Test: MySQL 表、菜单及关键模块表查询

**Interfaces:**

- Consumes: MySQL `root/root`、`sql/mysql` 目录与 PowerShell `Expand-Archive`。
- Produces: 新建的 `ruoyi-vue-pro` 数据库、`backup/ruoyi-vue-pro-<timestamp>.sql`、`work/sql` 解压目录。

- [ ] **Step 1: 记录数据库破坏性操作的失败保护**

```powershell
$databaseName = 'ruoyi-vue-pro'
mysql -uroot -proot -e "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='$databaseName';"
throw '未提供 -ConfirmReset 时不得删除数据库。'
```

Expected: 未传确认参数时停止，不删除任何数据库。

- [ ] **Step 2: 写入初始化脚本并要求显式确认**

```powershell
param(
    [switch]$ConfirmReset
)

$ErrorActionPreference = 'Stop'
$databaseName = 'ruoyi-vue-pro'
$databaseUser = 'root'
$databasePassword = 'root'
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$backupDirectory = Join-Path $PSScriptRoot 'backup'
$sqlDirectory = Join-Path $PSScriptRoot '..\..\sql\mysql'
$workDirectory = Join-Path $PSScriptRoot 'work\sql'

if (-not $ConfirmReset) {
    throw '数据库重建需要显式传入 -ConfirmReset。'
}

New-Item -ItemType Directory -Force $backupDirectory, $workDirectory | Out-Null
mysqldump -u$databaseUser -p$databasePassword --databases $databaseName |
    Set-Content -Encoding utf8 (Join-Path $backupDirectory "$databaseName-$timestamp.sql")
mysql -u$databaseUser -p$databasePassword -e "DROP DATABASE IF EXISTS ``$databaseName``; CREATE DATABASE ``$databaseName`` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
Get-Content -Raw (Join-Path $sqlDirectory 'ruoyi-vue-pro.sql') |
    & mysql -u$databaseUser -p$databasePassword $databaseName
Get-Content -Raw (Join-Path $sqlDirectory 'quartz.sql') |
    & mysql -u$databaseUser -p$databasePassword $databaseName
```

- [ ] **Step 3: 追加模块 SQL 的唯一最新包映射**

在脚本中定义以下映射，并在导入前用 `Test-Path` 校验每个文件存在；压缩包解压到 `work/sql/<module>` 后，
按压缩包内唯一 `.sql` 文件导入：

```powershell
$moduleSqlArchives = @{
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
```

在写入前先列出 `sql/mysql` 的报告模块文件；如果存在 `report-*.sql.zip`，以日期最大的唯一文件加入映射，
否则报告模块只使用基础 SQL 中已有表与菜单。此项是基于仓库实际文件列表的条件分支，不得猜测不存在的文件名。

- [ ] **Step 4: 运行脚本并验证导入结果**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File script/local-full/initialize-database.ps1 -ConfirmReset`

Expected: 生成带时间戳备份；基础 SQL、Quartz SQL 和 12 个模块 SQL 均返回零退出码。

- [ ] **Step 5: 执行数据库验收查询**

```powershell
mysql -uroot -proot -D ruoyi-vue-pro -e "SHOW TABLES LIKE 'system_users'; SHOW TABLES LIKE 'bpm_%'; SHOW TABLES LIKE 'ai_%'; SHOW TABLES LIKE 'iot_%'; SHOW TABLES LIKE 'im_%';"
mysql -uroot -proot -D ruoyi-vue-pro -e "SELECT COUNT(*) AS menu_count FROM system_menu;"
```

Expected: 基础表和各模块前缀表存在，`menu_count` 大于零。

## Task 3: 启用 Reactor 与服务器业务依赖

**Files:**

- Modify: `pom.xml:18-31`
- Modify: `yudao-server/pom.xml:36-141`
- Test: Maven Reactor 依赖解析和编译

**Interfaces:**

- Consumes: 所有 `yudao-module-*` 聚合模块与商城、IoT 子模块的已有 POM 关系。
- Produces: 含全部模块的 Maven Reactor 和可执行的 `yudao-server` Jar。

- [ ] **Step 1: 编写失败构建检查**

Run: `mvn -pl yudao-server -am -DskipTests compile`

Expected: 默认配置仅解析系统与基础设施，不能产出全模块组件。

- [ ] **Step 2: 取消根 POM 中全部业务模块的注释**

将下列 `<module>` 行解除注释，保持现有顺序：

```xml
<module>yudao-module-member</module>
<module>yudao-module-bpm</module>
<module>yudao-module-report</module>
<module>yudao-module-mp</module>
<module>yudao-module-pay</module>
<module>yudao-module-mall</module>
<module>yudao-module-crm</module>
<module>yudao-module-erp</module>
<module>yudao-module-iot</module>
<module>yudao-module-mes</module>
<module>yudao-module-wms</module>
<module>yudao-module-im</module>
<module>yudao-module-ai</module>
```

- [ ] **Step 3: 取消 Server POM 中全部业务依赖的注释**

解除会员、报表、BPM、支付、公众号、CRM、ERP、AI、IoT、MES、WMS、IM 的全部 `<dependency>` 块；
同时解除商城的 `yudao-module-product`、`yudao-module-promotion`、`yudao-module-trade` 和
`yudao-module-statistics` 依赖块。每个依赖保留 `cn.iocoder.boot` groupId、`${revision}` version。

- [ ] **Step 4: 运行全模块编译**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File script/local-full/start-backend.ps1 -SkipTests`

Expected: Maven 编译完成，构建日志出现全部业务模块名称，且没有 “Could not find artifact” 或 Bean 定义冲突。

- [ ] **Step 5: 运行模块测试**

Run: `$env:JAVA_HOME='D:\jdk-25.0.2'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; mvn -pl yudao-server -am test`

Expected: 测试通过；任何失败应保留完整 Maven 输出并先定位，不删除模块回避失败。

## Task 4: 新增无外部凭据的全模块本地 Profile

**Files:**

- Create: `yudao-server/src/main/resources/application-all-local.yaml`
- Test: Spring Boot 配置绑定、启动日志和健康端点

**Interfaces:**

- Consumes: `application.yaml` 的共享配置和 `application-local.yaml` 的本机 MySQL、Redis 地址。
- Produces: `all-local` profile；只依赖 MySQL、Redis 的全模块可启动配置。

- [ ] **Step 1: 写入最小 datasource 与 Redis 配置**

```yaml
server:
    port: 48080

spring:
    datasource:
        dynamic:
            datasource:
                master:
                    url: jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true
                    username: root
                    password: root
    data:
        redis:
            host: 127.0.0.1
            port: 6379
            database: 0
```

- [ ] **Step 2: 配置外部服务自动装配排除项**

```yaml
spring:
    autoconfigure:
        exclude:
            - org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreAutoConfiguration
            - org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration
            - org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration
            - com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatAutoConfiguration
            - com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAgentAutoConfiguration
            - com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeImageAutoConfiguration
            - com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeVideoAutoConfiguration
            - com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAudioSpeechAutoConfiguration
            - com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAudioTranscriptionAutoConfiguration
            - com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeRerankAutoConfiguration
            - com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeEmbeddingAutoConfiguration
            - com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeMultimodalEmbeddingAutoConfiguration
            - com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAsyncToolCallingManagerAutoConfiguration
```

在同一 profile 中关闭 TDengine 数据源，保持 WebSocket 的 `sender-type: local`，并将不在本机运行的
RocketMQ、RabbitMQ、Kafka 消费者配置为不自动连接。不得把 `yudao.module.*` 的组件扫描或菜单功能关闭。

- [ ] **Step 3: 运行配置失败检查**

Run: `mvn -pl yudao-server -am spring-boot:run -Dspring-boot.run.profiles=all-local`

Expected: 修改前，缺少外部服务会导致连接或自动装配失败；记录第一个失败栈。

- [ ] **Step 4: 启动并验证健康端点**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File script/local-full/start-backend.ps1 -SkipTests`

Run: `Invoke-WebRequest http://127.0.0.1:48080/actuator/health -UseBasicParsing`

Expected: HTTP 200，响应 JSON 的 `status` 为 `UP`；日志显示服务监听 48080。

- [ ] **Step 5: 验证登录与 API 文档入口**

Run: `Invoke-WebRequest http://127.0.0.1:48080/swagger-ui -UseBasicParsing`

Expected: HTTP 200；使用基础 SQL 的管理员凭据完成一次登录请求并得到令牌。

## Task 5: 恢复完整 Vue3 管理后台并合并 MES 覆盖文件

**Files:**

- Create: `script/local-full/restore-vue3-admin.ps1`
- Modify: `yudao-ui/yudao-ui-admin-vue3/`
- Test: `package.json`、依赖安装、Vite 开发服务器和生产构建

**Interfaces:**

- Consumes: 官方 `https://gitee.com/yudaocode/yudao-ui-admin-vue3.git`、现有 MES API 和页面文件。
- Produces: 可安装、可运行、可构建的 Vue3 工程，保留本地 MES 覆盖层。

- [ ] **Step 1: 验证现有目录无法构建**

Run: `Test-Path yudao-ui/yudao-ui-admin-vue3/package.json`

Expected: `False`，确认必须恢复官方完整基线。

- [ ] **Step 2: 写入安全恢复脚本**

```powershell
$ErrorActionPreference = 'Stop'
$targetDirectory = Join-Path $PSScriptRoot '..\..\yudao-ui\yudao-ui-admin-vue3'
$backupDirectory = Join-Path $PSScriptRoot ("backup\vue3-overlay-" + (Get-Date -Format 'yyyyMMdd-HHmmss'))
$sourceDirectory = Join-Path $PSScriptRoot 'work\yudao-ui-admin-vue3'

New-Item -ItemType Directory -Force $backupDirectory, (Split-Path $sourceDirectory) | Out-Null
Copy-Item -Recurse -Force $targetDirectory\src $backupDirectory
git clone --depth 1 https://gitee.com/yudaocode/yudao-ui-admin-vue3.git $sourceDirectory
Copy-Item -Recurse -Force $sourceDirectory\* $targetDirectory
Copy-Item -Recurse -Force $backupDirectory\src\api\mes $targetDirectory\src\api\mes
Copy-Item -Recurse -Force $backupDirectory\src\views\mes $targetDirectory\src\views\mes
```

脚本执行前应在源目录不存在时运行；若已经存在，先停止并提示人工清理，避免无意覆盖已恢复的完整工程。

- [ ] **Step 3: 运行恢复脚本并检查基线文件**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File script/local-full/restore-vue3-admin.ps1`

Expected: `package.json`、`vite.config.*`、`src/router`、`src/store` 存在；`src/api/mes` 和 `src/views/mes`
仍包含当前保留的文件。

- [ ] **Step 4: 安装依赖并验证生产构建**

Run: `npm install`

Run: `npm run build`

Expected: 安装成功且生成 `dist`；没有 Vue2、Element UI 或 TypeScript API 导入错误。

- [ ] **Step 5: 记录前端变更清单**

Run: `Get-ChildItem yudao-ui/yudao-ui-admin-vue3 -Force | Select-Object Name`

Expected: 完整工程文件与构建产物可见；不执行 Git 提交，因为工作目录没有 `.git`。

## Task 6: 启动前端并执行全模块验收

**Files:**

- Create: `script/local-full/start-frontend.ps1`
- Test: 浏览器访问、登录、动态菜单、模块路由和 API 响应

**Interfaces:**

- Consumes: 已启动的 `http://127.0.0.1:48080` 后端和完成安装的 Vue3 工程。
- Produces: 本地可访问的管理后台及全模块验收记录。

- [ ] **Step 1: 写入前端启动脚本**

```powershell
$ErrorActionPreference = 'Stop'
$frontendDirectory = Join-Path $PSScriptRoot '..\..\yudao-ui\yudao-ui-admin-vue3'

Push-Location $frontendDirectory
try {
    if (-not (Test-Path 'node_modules')) {
        npm install
    }
    npm run dev
}
finally {
    Pop-Location
}
```

- [ ] **Step 2: 验证前后端代理**

Run: `powershell -NoProfile -ExecutionPolicy Bypass -File script/local-full/start-frontend.ps1`

Expected: Vite 显示本地访问 URL；打开该 URL 后登录请求通过 `/admin-api` 代理到 48080，没有跨域错误。

- [ ] **Step 3: 执行菜单与路由验收**

使用基础 SQL 中的管理员账号登录，确认侧边栏至少包含系统管理、基础设施、会员中心、工作流、报表、公众号、
支付、商城、CRM、ERP、AI、IoT、MES、WMS、IM。逐个打开模块的首个列表页。

Expected: 每个页面返回 HTTP 200 或业务空列表；不得出现前端路由找不到、后端 404、数据库表不存在或
`NoSuchBeanDefinitionException`。

- [ ] **Step 4: 验证外部功能的降级边界**

分别触发一次 AI 模型调用、支付下单、微信公众号同步、IoT 网关连接和 IM 实时服务入口。

Expected: 页面与接口可达；未配置服务时返回明确配置缺失或业务异常，不导致服务退出、线程持续重连或整体
健康检查失败。

- [ ] **Step 5: 汇总验收证据**

记录 Java 25、Maven 构建、数据库导入、`/actuator/health`、前端构建、管理员登录、所有模块菜单的命令
输出或截图路径。只有所有关键检查通过后，才标记本计划完成。

## Self-Review

- Spec coverage: Task 1 覆盖 JDK 和服务前置条件；Task 2 覆盖备份、重建与 SQL；Task 3 覆盖模块装配；
  Task 4 覆盖外部依赖降级；Task 5 覆盖 Vue3 基线和 MES 合并；Task 6 覆盖端到端验收和回退证据。
- Placeholder scan: 本计划未使用 `TODO`、`TBD`、`implement later` 或未指定路径的实施步骤。
- Type consistency: 所有脚本、profile、数据库名、服务端口和前端目录均使用相同的精确名称。
