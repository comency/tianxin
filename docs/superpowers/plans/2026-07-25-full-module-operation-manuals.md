# Full Module Operation Manuals Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 启动当前全模块 RuoYi-Vue-Pro 项目，核验可用功能，并在 `docs/` 交付完整中文操作手册。

**Architecture:** 先核验既有前后端启动方式与运行状态，再以动态菜单、Vue 页面、前端 API 和后端控制器为事实来源编写 Markdown。文档按模块拆分，以通用说明消除重复，并对外部集成和运行环境缺失的能力明确标注前置条件。

**Tech Stack:** JDK 25、Spring Boot、Vue 3、Vite、PowerShell、Markdown、UTF-8。

## Global Constraints

- 所有新建文档使用 UTF-8 编码，中文内容不使用异常编码字符或不可见字符。
- 文档只描述经源码、菜单、接口或运行页面核验的功能；不修改业务代码、数据库和运行配置。
- 先检查 `48080` 和 `80` 端口；未监听时使用 `start_all.bat` 或项目内既有脚本启动。
- 前端入口为 `yudao-ui/yudao-ui-admin-vue3`，后端入口为 `yudao-server`。
- 输出文件固定在 `docs/`；当前目录无 `.git`，不执行提交操作。
- 对支付、公众号、AI、IoT、IM 等外部依赖功能，必须写明启用前提和当前不可用时的核查方式。

---

## File Structure

- Create: `docs/README.md`：所有手册的入口、阅读顺序、适用角色和维护原则。
- Create: `docs/00-使用前准备与通用约定.md`：访问、登录、权限、列表、表单、导入导出和审批通用规则。
- Create: `docs/01-系统功能操作手册.md`：系统管理和基础设施功能。
- Create: `docs/02-ERP说明及操作手册.md`：ERP 主数据、采购、销售、库存和财务业务闭环。
- Create: `docs/03-工作流操作手册.md` 至 `docs/14-即时通讯操作手册.md`：各独立业务模块操作手册。
- Modify: `docs/superpowers/plans/2026-07-25-full-module-operation-manuals.md`：记录启动与验证事实。

### Task 1: 核验项目启动状态与功能事实来源

**Files:**
- Read: `start_all.bat`
- Read: `yudao-ui/yudao-ui-admin-vue3/package.json`
- Read: `yudao-server/src/main/resources/application-all-local.yaml`
- Read: `logs/backend-current.out.log`
- Modify: `docs/superpowers/plans/2026-07-25-full-module-operation-manuals.md`

**Interfaces:**
- Consumes: 项目现有前后端启动脚本、端口和日志。
- Produces: 已验证的访问地址、登录前提、可运行模块范围和环境限制，供后续文档引用。

- [ ] **Step 1: 检查监听端口和已运行进程**

```powershell
Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
    Where-Object { $_.LocalPort -in 48080, 80, 5173, 3000 } |
    Select-Object LocalAddress, LocalPort, OwningProcess
Get-Process -Name java, node -ErrorAction SilentlyContinue |
    Select-Object Id, ProcessName, StartTime
```

Expected: 输出当前监听状态；不将存在的 Java 或 Node 进程误判为本项目服务。

- [ ] **Step 2: 读取项目约定的启动命令**

```powershell
Get-Content -Raw start_all.bat
Get-Content -Raw yudao-ui/yudao-ui-admin-vue3/package.json
Get-Content -Raw yudao-server/src/main/resources/application-all-local.yaml
```

Expected: 明确后端 profile、前端 `dev` 脚本及代理地址。

- [ ] **Step 3: 在服务未监听时启动项目并记录日志路径**

```powershell
Start-Process -FilePath cmd.exe -ArgumentList '/c', 'start_all.bat' -WorkingDirectory (Get-Location) -WindowStyle Hidden
```

Expected: 后端监听 `48080`；前端开发服务输出实际 URL。若启动失败，保留日志并在文档中记录限制。

- [ ] **Step 4: 验证健康接口与前端入口**

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:48080/actuator/health
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:80/
```

Expected: 返回 HTTP 200。前端端口不同则使用启动日志中的实际端口重试。

- [ ] **Step 5: 记录实际验证结果**

在本任务下追加端口、启动时间、日志路径与无法验证的环境原因。

Expected: 后续手册能准确区分已运行功能和未满足环境条件。

### Task 2: 编写入口、通用和系统管理操作手册

**Files:**
- Create: `docs/README.md`
- Create: `docs/00-使用前准备与通用约定.md`
- Create: `docs/01-系统功能操作手册.md`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/system/`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/infra/`

**Interfaces:**
- Consumes: Task 1 的地址、登录和运行限制；系统和基础设施菜单、页面与 API。
- Produces: 所有模块共用的操作约定和系统管理员操作说明。

- [ ] **Step 1: 列出系统与基础设施页面和 API 文件**

```powershell
Get-ChildItem yudao-ui/yudao-ui-admin-vue3/src/views/system -Directory | Select-Object -ExpandProperty Name
Get-ChildItem yudao-ui/yudao-ui-admin-vue3/src/views/infra -Directory | Select-Object -ExpandProperty Name
Get-ChildItem yudao-ui/yudao-ui-admin-vue3/src/api/system -File | Select-Object -ExpandProperty Name
Get-ChildItem yudao-ui/yudao-ui-admin-vue3/src/api/infra -File | Select-Object -ExpandProperty Name
```

Expected: 得到用户、组织、权限、字典、参数、日志、文件、任务和代码生成的实际功能名。

- [ ] **Step 2: 编写目录和通用约定**

`docs/README.md` 与 `docs/00-使用前准备与通用约定.md` 必须包含以下章节：

```markdown
## 阅读说明
## 访问与登录
## 菜单与数据权限
## 列表、筛选与分页
## 新增、编辑、删除与状态变更
## 导入、导出与附件
## 审批与消息通知
## 数据安全与问题反馈
```

Expected: 给出通用操作、权限前提和不可逆动作提示，不虚构默认账号密码。

- [ ] **Step 3: 编写系统功能操作手册**

`docs/01-系统功能操作手册.md` 覆盖组织和用户、角色和菜单、租户、字典和配置、通知、日志、文件、任务、接口和数据库工具、代码生成。每节使用“菜单路径、操作步骤、校验结果、注意事项”小节。

Expected: 危险管理操作包含影响范围。

- [ ] **Step 4: 校验入口、编码和覆盖范围**

```powershell
Get-Content -Raw -Encoding utf8 docs/README.md
Get-Content -Raw -Encoding utf8 docs/00-使用前准备与通用约定.md
Get-Content -Raw -Encoding utf8 docs/01-系统功能操作手册.md
```

Expected: 中文标题和链接正常显示，README 指向三份已存在手册。

### Task 3: 编写 ERP、CRM、商城、会员与支付操作手册

**Files:**
- Create: `docs/02-ERP说明及操作手册.md`
- Create: `docs/04-会员操作手册.md`
- Create: `docs/05-支付操作手册.md`
- Create: `docs/06-商城操作手册.md`
- Create: `docs/07-CRM操作手册.md`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/erp/`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/crm/`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/mall/`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/member/`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/pay/`

**Interfaces:**
- Consumes: 通用文档的列表、表单和权限规则；各模块页面、路由和 API 定义。
- Produces: 面向销售、采购、财务、客户运营和商城运营人员的操作流程。

- [ ] **Step 1: 提取五个模块的页面目录和 API 名称**

```powershell
'erp', 'crm', 'mall', 'member', 'pay' | ForEach-Object {
    Get-ChildItem "yudao-ui/yudao-ui-admin-vue3/src/views/$_" -Directory |
        Select-Object @{Name = 'Module'; Expression = { $_ }}, Name
}
```

Expected: 手册章节名称与仓库实际业务域一致。

- [ ] **Step 2: 编写 ERP 端到端业务闭环**

`docs/02-ERP说明及操作手册.md` 必须按“基础资料 -> 采购或销售订单 -> 审核 -> 入库或出库 -> 应收或应付 -> 收款或付款 -> 库存和资金核对”描述；每环节说明前置状态、可执行动作、成功结果和禁止操作。

Expected: 读者能按顺序完成可追溯业务单据链。

- [ ] **Step 3: 编写 CRM、商城、会员和支付手册**

CRM 覆盖线索至合同和回款；商城覆盖商品、营销、订单、发货和售后；会员覆盖用户、等级、积分、标签和签到；支付覆盖商户、应用、渠道、支付/退款/转账订单和对账。

Expected: 支付渠道和商城配送均标注必要的外部配置，不承诺未配置渠道可交易。

- [ ] **Step 4: 静态核验五份文件**

```powershell
Get-ChildItem docs -File -Filter '*操作手册.md' | Where-Object {
    $_.Name -match 'ERP|会员|支付|商城|CRM'
} | Select-Object Name, Length
```

Expected: 输出五个非空 Markdown 文件。

### Task 4: 编写工作流、报表、公众号、AI、IoT 与即时通讯操作手册

**Files:**
- Create: `docs/03-工作流操作手册.md`
- Create: `docs/08-报表操作手册.md`
- Create: `docs/09-公众号操作手册.md`
- Create: `docs/10-AI操作手册.md`
- Create: `docs/11-IoT操作手册.md`
- Create: `docs/14-即时通讯操作手册.md`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/bpm/`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/report/`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/mp/`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/ai/`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/iot/`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/im/`

**Interfaces:**
- Consumes: Task 1 的服务限制和 Task 2 的权限、附件、消息约定。
- Produces: 协同、内容、智能和设备运营模块的角色化操作说明。

- [ ] **Step 1: 收集实际页面功能名**

```powershell
'bpm', 'report', 'mp', 'ai', 'iot', 'im' | ForEach-Object {
    Get-ChildItem "yudao-ui/yudao-ui-admin-vue3/src/views/$_" -Directory -ErrorAction Stop |
        Select-Object @{Name = 'Module'; Expression = { $_ }}, Name
}
```

Expected: 目录缺失时转向同名 API 和后端 Controller 核验并在手册记录。

- [ ] **Step 2: 编写流程、报表与公众号手册**

工作流说明模型发布、流程发起、待办处理、已办查询、撤回和驳回；报表说明数据集、报表与大屏设计、发布和浏览；公众号说明账号、菜单、素材、用户、消息和自动回复。

Expected: 管理员配置和日常操作分开描述，审批权限职责明确。

- [ ] **Step 3: 编写 AI、IoT 与即时通讯手册**

三份文档均先写“启用条件”，再描述配置、日常操作和监控；必须列出模型密钥、设备协议网关、数据库表和音视频服务等依赖。

Expected: 未满足依赖时，读者能据此定位而不是误操作。

- [ ] **Step 4: 检查 UTF-8 与文档标题**

```powershell
Get-ChildItem docs -File -Filter '*操作手册.md' | ForEach-Object {
    $content = Get-Content -Raw -Encoding utf8 $_.FullName
    [PSCustomObject]@{ Name = $_.Name; Length = $content.Length; HasTitle = $content.StartsWith('# ') }
}
```

Expected: 每份文档 `Length` 大于零且 `HasTitle` 为 `True`。

### Task 5: 编写 MES、WMS 手册并执行全量交付核验

**Files:**
- Create: `docs/12-MES操作手册.md`
- Create: `docs/13-WMS操作手册.md`
- Modify: `docs/README.md`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/mes/`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/wms/`

**Interfaces:**
- Consumes: ERP 主数据、审批、库存和单据状态规则。
- Produces: 制造与仓储业务操作手册，以及经过核验的最终入口文档。

- [ ] **Step 1: 收集 MES 和 WMS 的实际页面**

```powershell
Get-ChildItem yudao-ui/yudao-ui-admin-vue3/src/views/mes -Directory | Select-Object -ExpandProperty Name
Get-ChildItem yudao-ui/yudao-ui-admin-vue3/src/views/wms -Directory | Select-Object -ExpandProperty Name
```

Expected: 确认基础资料、生产、入库、出库、移库和盘点的功能边界。

- [ ] **Step 2: 编写标准作业流程**

MES 和 WMS 均按“基础资料 -> 单据创建 -> 确认或审核 -> 库存影响 -> 追溯查询”编写；MES 突出生产过程，WMS 突出收货、发货、移动和盘点。

Expected: 两份手册交叉引用 ERP 产品和库存基础资料，不重复定义相同术语。

- [ ] **Step 3: 补齐 README 模块索引**

README 必须链接 `00` 至 `14` 的所有本次手册，并提供系统管理员、ERP 操作员、仓储/制造操作员、销售/客户运营人员、集成管理员的阅读路线。

Expected: 全部链接使用相对路径，目标文件均存在。

- [ ] **Step 4: 执行完整性、链接和占位符检查**

```powershell
$required = @(
    'README.md', '00-使用前准备与通用约定.md', '01-系统功能操作手册.md', '02-ERP说明及操作手册.md',
    '03-工作流操作手册.md', '04-会员操作手册.md', '05-支付操作手册.md', '06-商城操作手册.md',
    '07-CRM操作手册.md', '08-报表操作手册.md', '09-公众号操作手册.md', '10-AI操作手册.md',
    '11-IoT操作手册.md', '12-MES操作手册.md', '13-WMS操作手册.md', '14-即时通讯操作手册.md'
)
$required | ForEach-Object {
    $path = Join-Path 'docs' $_
    [PSCustomObject]@{ File = $_; Exists = Test-Path $path; Bytes = if (Test-Path $path) { (Get-Item $path).Length } else { 0 } }
}
rg -n 'TODO|TBD|待补充|待完善|待定' docs/README.md docs/00-* docs/01-* docs/02-* docs/03-* docs/04-* docs/05-* docs/06-* docs/07-* docs/08-* docs/09-* docs/10-* docs/11-* docs/12-* docs/13-* docs/14-*
```

Expected: 每个文件存在且非空；占位符扫描无输出。

## 实际验证结果

- 2026-07-25：MySQL 查询与 Redis `PONG` 预检通过。
- 2026-07-25：首次执行 `start_all.bat` 时，Maven 清理 `yudao-module-bpm/target` 出现一次临时删除失败；独立执行同一模块的 `clean` 随后成功，目录权限正常。
- 2026-07-25：后端 `http://127.0.0.1:48080/actuator/health` 返回 HTTP 200，状态为 `UP`。
- 2026-07-25：前端 Vite 以 `env.local` 模式监听 `http://localhost:80/`；首页返回 HTTP 200。
- 2026-07-25：`docs/README.md` 与 00 至 14 共 16 份手册均存在、非空、可按 UTF-8 读取，索引链接均可解析，未发现占位符。

## Plan Self-Review

- Spec coverage: Task 1 覆盖启动、运行验证和环境限制；Task 2 覆盖入口、通用与系统功能；Task 3 覆盖 ERP、CRM、商城、会员与支付；Task 4 覆盖 BPM、报表、公众号、AI、IoT 与 IM；Task 5 覆盖 MES、WMS、索引、文件完整性和 UTF-8 检查。
- Placeholder scan: 本计划不含待实现内容；命令、路径、预期结果和文档章节均已明确。
- Interface consistency: 后续模块手册依赖 Task 1 的运行事实和 Task 2 的通用约定；Task 5 汇总并链接 Task 2 至 Task 4 的文件，路径一致。
