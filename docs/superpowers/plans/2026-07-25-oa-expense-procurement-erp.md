# OA 费用采购与 ERP 交接 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 交付费用报销、借款、采购申请与 BPM 审批，并以幂等、可重试、可审计的方式向 ERP 交接。

**Architecture:** OA 保存申请、明细和交接记录，BPM 返回流程终态，ERP 通过新增公开 API 接收命令。OA 不写 ERP 订单或付款表；交接记录是唯一的重试入口，`idempotency_key` 是跨系统去重键。

**Tech Stack:** Java 25、Spring Boot、MyBatis-Plus、Flowable、Vue 3、TypeScript、ERP 模块 API、MySQL 8。

## Global Constraints

- 所有接口、SQL、日志和页面文案使用 UTF-8；金额采用 `BigDecimal`，禁止 `double`。
- 报销、借款和采购业务表与 BPM、ERP 内部表隔离，只通过公开 API 或领域事件通信。
- 所有 ERP 命令携带稳定的 `idempotencyKey`，重试不得新建第二张 ERP 单据。
- 流程通过前不得写 ERP；流程驳回、撤回或作废必须停止或取消未发送交接。
- 当前目录无 Git 仓库；恢复 Git 后按任务提交。

---

### Task 1: 费用、借款、采购单据和 BPM 回写

**Files:**
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/expense/**`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/procurement/**`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/**/listener/**`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/controller/admin/expense/**`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/controller/admin/procurement/**`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/dal/**/OaExpense*.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/dal/**/OaLoan*.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/dal/**/OaPurchaseRequest*.java`
- Test: `yudao-module-oa/src/test/java/cn/iocoder/yudao/module/oa/service/expense/OaExpenseServiceImplTest.java`

**Interfaces:**
- Consumes: `BpmProcessInstanceApi` and terminal status events.
- Produces: business keys `oa_expense`、`oa_loan`、`oa_purchase_request`; all applications include `processInstanceId` and terminal OA status.

- [ ] **Step 1: 编写审批后交接创建失败测试**

```java
@Test
void approveExpense_shouldCreateOnePendingErpHandoff() {
    statusListener.onApplicationEvent(approvedExpenseEvent);
    assertThat(handoffMapper.selectByBusiness(EXPENSE, expenseId)).hasSize(1);
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaExpenseServiceImplTest`

Expected: FAIL because expense service, listener and handoff mapper do not exist.

- [ ] **Step 3: 实现单据、明细和流程监听器**

报销单必须保存费用类型、发生日期、金额、附件、分摊和明细；借款必须保存可核销余额；采购申请必须保存申请部门、物品明细和预算校验结果。创建时校验非负金额、明细汇总、附件、重复报销和流程映射。终态监听器仅在“通过”时创建唯一 `oa_erp_handoff` 记录，状态为 `PENDING`；其唯一索引为 `(tenant_id, business_type, business_id, target_system)`。

- [ ] **Step 4: 运行测试**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaExpenseServiceImplTest`

Expected: PASS for approval, rejection, withdrawal, duplicate terminal event, duplicate expense evidence and loan settlement balance.

- [ ] **Step 5: Commit**

```bash
git add yudao-module-oa sql/mysql
git commit -m "feat(oa): add expense loan and procurement applications"
```

### Task 2: ERP 公开命令 API 与幂等交接执行器

**Files:**
- Create: `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/api/oa/OaErpHandoffApi.java`
- Create: `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/api/oa/dto/OaExpenseHandoffReqDTO.java`
- Create: `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/api/oa/dto/OaPurchaseHandoffReqDTO.java`
- Create: `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/api/oa/OaErpHandoffApiImpl.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/job/OaErpHandoffJob.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/integration/OaErpHandoffService.java`
- Test: `yudao-module-oa/src/test/java/cn/iocoder/yudao/module/oa/service/integration/OaErpHandoffServiceImplTest.java`

**Interfaces:**
- Produces: `Long createExpensePayable(OaExpenseHandoffReqDTO request)` and `Long createPurchaseOrderDraft(OaPurchaseHandoffReqDTO request)`.
- Requires: both DTOs contain `String idempotencyKey`, `Long sourceBusinessId`, `Long applicantUserId`, `Long deptId`, `BigDecimal amount` and line items.

- [ ] **Step 1: 写幂等失败测试**

```java
@Test
void executeHandoffTwice_shouldReturnSameErpDocumentId() {
    Long first = handoffService.execute(handoffId);
    Long second = handoffService.execute(handoffId);
    assertThat(second).isEqualTo(first);
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaErpHandoffServiceImplTest`

Expected: FAIL because the OA-to-ERP command API and executor do not exist.

- [ ] **Step 3: 实现 ERP 命令边界和执行器**

在 ERP 模块实现 API 前，确认现有采购、付款领域公开服务可接受 DTO；若不存在，在 ERP 内部创建最小公开 API，不暴露 Mapper。ERP 侧以 `idempotency_key` 查询历史命令，存在则返回原 ERP 单据编号；不存在则原子创建采购订单草稿或费用应付草稿并持久化幂等映射。OA 执行器只锁定 `PENDING` 或到期 `RETRYING` 记录，调用成功转 `SUCCEEDED` 并保存 ERP 单据号；可恢复错误转 `RETRYING`，业务校验错误转 `FAILED` 并停止自动重试。

- [ ] **Step 4: 运行集成测试**

Run: `mvn -pl yudao-module-oa,yudao-module-erp -am test -Dtest=OaErpHandoffServiceImplTest`

Expected: PASS for first send, repeated send, timeout retry, permanent validation failure and ERP duplicate callback.

- [ ] **Step 5: Commit**

```bash
git add yudao-module-oa yudao-module-erp
git commit -m "feat(oa): add idempotent ERP handoff"
```

### Task 3: 财务采购前端、审计与发布验证

**Files:**
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/oa/expense/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/oa/procurement/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/expense/**`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/procurement/**`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/integration/handoff/index.vue`
- Test: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/expense/__tests__/expense-form.spec.ts`

**Interfaces:**
- Consumes: expense, loan, procurement and handoff REST APIs from Tasks 1-2.
- Produces: users can see application status and ERP handoff status; retry button is restricted to `oa:integration:retry`.

- [ ] **Step 1: 编写金额与幂等状态失败测试**

```ts
it('does not submit when detail totals differ from the declared amount', async () => {
  await submit()
  expect(message.warning).toHaveBeenCalledWith('费用明细合计必须等于报销金额')
})
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `pnpm --dir yudao-ui/yudao-ui-admin-vue3 test expense-form.spec.ts`

Expected: FAIL because expense form page does not exist.

- [ ] **Step 3: 实现类型化表单和交接审计页**

费用、借款和采购页面必须使用 `BigDecimal` 对应的字符串金额输入格式，提交前比较明细合计与总额。详情展示 OA 单据、BPM 流程编号、ERP 单据编号、交接状态、重试次数和失败原因；普通申请人不可见请求或响应摘要。失败重试按钮只对集成人员显示并要求二次确认。

- [ ] **Step 4: 执行完整验证**

Run: `mvn -pl yudao-module-oa,yudao-module-erp -am test`

Expected: `BUILD SUCCESS`.

Run: `pnpm --dir yudao-ui/yudao-ui-admin-vue3 lint && pnpm --dir yudao-ui/yudao-ui-admin-vue3 typecheck`

Expected: exit code 0.

Manual: 提交并审批报销、借款和采购；确认每项仅生成一个 ERP 草稿或应付单；模拟超时后重试，确认 ERP 单据编号不变且审计记录完整。

- [ ] **Step 5: Commit**

```bash
git add yudao-ui/yudao-ui-admin-vue3 yudao-module-oa yudao-module-erp
git commit -m "feat(oa): add expense procurement and ERP handoff views"
```
