# 全模块图文操作手册优化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 RuoYi-Vue-Pro 的所有现有模块手册补全关键业务流程、真实管理端截图和可复核的操作说明。

**Architecture:** 以 Vue3 管理端实际菜单和页面为事实来源，将截图统一放在 `docs/assets/screenshots/` 的模块目录。每个手册按“前置条件、菜单路径、步骤、截图、核验、异常”结构扩写，并通过 README 建立统一入口。

**Tech Stack:** Markdown、PNG、Vue3 管理端、PowerShell、浏览器自动化。

## Global Constraints

- 所有文档和图片引用使用 UTF-8 与相对路径。
- 只使用本地运行的真实管理端页面截图，不生成界面示意图。
- 不写入账户密码、令牌、密钥和真实个人敏感信息。
- 不改动业务代码、数据库记录或运行配置。
- 页面无演示数据时，记录数据前置条件并截取真实的列表或表单页面。

---

### Task 1: 建立流程与截图清单

**Files:**
- Create: `docs/assets/screenshots/README.md`
- Modify: `docs/README.md`
- Read: `yudao-ui/yudao-ui-admin-vue3/src/views/**`

- [ ] 核对各模块现有页面和菜单，确定每份手册的关键操作闭环。
- [ ] 在截图目录说明中登记图片目录、命名格式和脱敏要求。
- [ ] 在文档索引中标记图文手册和截图目录约定。
- [ ] 检查 README 内所有链接可解析。

### Task 2: 采集平台与经营模块页面

**Files:**
- Create: `docs/assets/screenshots/01-system/`
- Create: `docs/assets/screenshots/03-bpm/`
- Create: `docs/assets/screenshots/04-member/`
- Create: `docs/assets/screenshots/05-pay/`
- Create: `docs/assets/screenshots/06-mall/`
- Create: `docs/assets/screenshots/07-crm/`
- Create: `docs/assets/screenshots/08-report/`
- Create: `docs/assets/screenshots/09-mp/`

- [ ] 登录本地管理端并按菜单进入每项关键流程页面。
- [ ] 截取列表、创建或编辑、处理或核验页面，确保对应操作步骤有图可查。
- [ ] 检查图片均为非空 PNG，文件名和目录符合约定。

### Task 3: 采集集成与智能模块页面

**Files:**
- Create: `docs/assets/screenshots/10-ai/`
- Create: `docs/assets/screenshots/11-iot/`
- Create: `docs/assets/screenshots/14-im/`

- [ ] 截取每项可执行配置、查询或处理流程的真实页面。
- [ ] 对依赖外部平台的功能记录截图所能覆盖的本地配置范围。
- [ ] 检查图片不包含密钥、令牌、二维码或个人敏感数据。

### Task 4: 采集供应链与制造模块页面

**Files:**
- Create: `docs/assets/screenshots/02-erp/`
- Create: `docs/assets/screenshots/12-mes/`
- Create: `docs/assets/screenshots/13-wms/`

- [ ] 按采购、销售、库存、财务、生产、质量、设备、收货、发货、移库和盘点等闭环采集页面。
- [ ] 对每个闭环至少保存一个真实页面截图，优先保存表单或处理页面。
- [ ] 检查截图与对应模块手册的业务术语一致。

### Task 5: 扩写手册并验证交付物

**Files:**
- Modify: `docs/00-使用前准备与通用约定.md`
- Modify: `docs/01-系统功能操作手册.md`
- Modify: `docs/02-ERP说明及操作手册.md`
- Modify: `docs/03-工作流操作手册.md`
- Modify: `docs/04-会员操作手册.md`
- Modify: `docs/05-支付操作手册.md`
- Modify: `docs/06-商城操作手册.md`
- Modify: `docs/07-CRM操作手册.md`
- Modify: `docs/08-报表操作手册.md`
- Modify: `docs/09-公众号操作手册.md`
- Modify: `docs/10-AI操作手册.md`
- Modify: `docs/11-IoT操作手册.md`
- Modify: `docs/12-MES操作手册.md`
- Modify: `docs/13-WMS操作手册.md`
- Modify: `docs/14-即时通讯操作手册.md`

- [ ] 为每个关键流程加入适用角色、前置条件、编号步骤、截图、结果核验和异常处理。
- [ ] 为页面截图添加中文图注和相对路径引用。
- [ ] 更新 `docs/README.md` 的覆盖说明和图文规范入口。
- [ ] 校验 16 份 Markdown 文档、所有图片引用和 README 链接均存在且非空。
- [ ] 复核前端和后端可访问，并抽查截图与页面内容一致。
