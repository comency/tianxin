# OA 基础与假勤 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增独立 OA 模块，并交付员工档案、假勤规则、假期余额、请假/加班/出差/补卡/外出申请及考勤异常闭环。

**Architecture:** `yudao-module-oa` 只保存 OA 领域业务表，组织和用户引用 `yudao-module-system`，审批通过 `BpmProcessInstanceApi` 发起并由状态事件监听器回写。假期余额采用预占、释放、正式扣减三段式账务，防止并发重复占用。

**Tech Stack:** Java 25、Spring Boot、MyBatis-Plus、Flowable、Vue 3、TypeScript、Element Plus、MySQL 8、Maven、pnpm。

## Global Constraints

- 所有新增 Java、TypeScript、Vue、SQL 与文档文件使用 UTF-8；Python 文件若新增必须包含 `# -*- coding: utf-8 -*-`。
- Java 使用 4 个空格缩进，单行不超过 120 字符；类和业务方法使用中文 Javadoc。
- OA 不复制 `system_user`、`system_dept`、BPM 与 ERP 的核心数据，不直接修改其他模块内部表。
- 菜单权限使用 `oa:<领域>:<动作>`；审批任务权限继续使用 BPM 权限。
- 所有状态写入、余额变动与集成调用必须具备事务、幂等键和操作审计。
- 当前目录没有 `.git`；执行前恢复 Git 仓库后按每个任务提交。未恢复前不得伪造提交记录。

---

## File Structure

| 路径 | 职责 |
| --- | --- |
| `yudao-module-oa/pom.xml` | OA 模块依赖定义 |
| `yudao-module-oa/src/main/java/.../oa/...` | 员工、假勤、申请与工作台领域代码 |
| `yudao-module-oa/src/test/java/.../oa/...` | 服务、余额和状态监听器测试 |
| `yudao-server/pom.xml`、根 `pom.xml` | 注册 OA Maven 模块 |
| `sql/mysql/oa-2026-07-25.sql` | 可重复执行的 OA 建表、字典和菜单增量脚本 |
| `sql/mysql/ruoyi-vue-pro.sql` | 全量初始化脚本同步 OA 基础数据 |
| `yudao-ui/yudao-ui-admin-vue3/src/api/oa/**` | OA 管理端 API 客户端与强类型 VO |
| `yudao-ui/yudao-ui-admin-vue3/src/views/oa/**` | OA 工作台、员工、假勤和申请页面 |

### Task 1: 建立 OA 模块、数据库基线与菜单

**Files:**
- Create: `yudao-module-oa/pom.xml`
- Create: `sql/mysql/oa-2026-07-25.sql`
- Modify: `pom.xml`
- Modify: `yudao-server/pom.xml`
- Modify: `sql/mysql/ruoyi-vue-pro.sql`
- Test: `yudao-module-oa/src/test/java/cn/iocoder/yudao/module/oa/OaModuleDependencyTest.java`

**Interfaces:**
- Consumes: `yudao-module-system`、`yudao-module-bpm`、`yudao-module-infra` 的公开 API。
- Produces: Maven artifact `cn.iocoder.boot:yudao-module-oa`，菜单根 `OA 协同办公` 与 `oa:*` 权限。

- [ ] **Step 1: 编写模块装配测试**

```java
class OaModuleDependencyTest {
    @Test
    void shouldExposeRequiredModuleApis() {
        assertNotNull(BpmProcessInstanceApi.class);
        assertNotNull(AdminUserApi.class);
    }
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaModuleDependencyTest -DfailIfNoTests=false`

Expected: Maven reports that module `yudao-module-oa` does not exist.

- [ ] **Step 3: 最小化实现模块和基线脚本**

在根 POM 的 `<modules>` 中加入 `yudao-module-oa`，在 Server POM 加入同名依赖。模块 POM 依赖 System、BPM、Infra、Web、Security、MyBatis、Test 与 Excel starter。增量 SQL 依次创建 `oa_employee_profile`、`oa_attendance_rule`、`oa_leave_balance`、`oa_attendance_record`、`oa_attendance_exception` 和 `oa_process_application`，全部含 `tenant_id`、`creator`、`create_time`、`updater`、`update_time`、`deleted`；为 `(tenant_id, user_id, leave_type)`、`(tenant_id, business_type, business_id)` 建唯一索引。插入一级菜单和工作台、员工档案、假勤规则、考勤管理、假期余额、发起申请、我的申请、审批中心、OA 设置菜单及查询/创建/更新/删除权限。

- [ ] **Step 4: 运行模块测试与 SQL 语法检查**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaModuleDependencyTest -DfailIfNoTests=false`

Expected: `BUILD SUCCESS` and one passing dependency test.

Run the script `sql/mysql/oa-2026-07-25.sql` once against the local MySQL database configured by `yudao-server/src/main/resources/application-local.yaml`, then execute it once more in the same database.

Expected: all OA tables, indexes, dictionaries, menus and permissions are created once; the second execution does not duplicate data.

- [ ] **Step 5: Commit**

```bash
git add pom.xml yudao-server/pom.xml yudao-module-oa sql/mysql
git commit -m "feat(oa): bootstrap module and attendance schema"
```

### Task 2: 员工档案、假勤规则与余额账本

**Files:**
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/attendance/OaLeaveBalanceService.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/attendance/OaLeaveBalanceServiceImpl.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/dal/dataobject/attendance/OaLeaveBalanceDO.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/dal/mysql/attendance/OaLeaveBalanceMapper.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/controller/admin/employee/**`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/controller/admin/attendance/**`
- Test: `yudao-module-oa/src/test/java/cn/iocoder/yudao/module/oa/service/attendance/OaLeaveBalanceServiceImplTest.java`

**Interfaces:**
- Consumes: `AdminUserApi` and `DeptApi` for validation only.
- Produces: `reserve(userId, leaveType, minutes, businessId)`、`confirm(businessId)`、`release(businessId)`; all methods are idempotent by business identifier.

- [ ] **Step 1: 写余额状态转换失败测试**

```java
@Test
void reserveConfirmRelease_shouldKeepAvailableBalanceCorrect() {
    leaveBalanceService.reserve(100L, 1, 480, 900L);
    leaveBalanceService.confirm(900L);
    assertThat(leaveBalanceMapper.selectByUserIdAndType(100L, 1).getAvailableMinutes()).isEqualTo(4320);
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaLeaveBalanceServiceImplTest`

Expected: FAIL because `OaLeaveBalanceService` and its mapper do not exist.

- [ ] **Step 3: 实现余额账本和管理端 CRUD**

实现余额行锁查询，预占时校验 `available_minutes >= minutes`，确认时将预占转已用，释放时仅释放尚未确认的预占。员工档案仅扩展 `user_id` 的入职日期、员工号、紧急联系人、证照和状态；用户、部门、岗位仍从 System 查询。假勤规则维护班次、工作日历、假期类型、年度额度和最小请假单位。Controller 使用 `@PreAuthorize("@ss.hasPermission('oa:attendance:query')")` 等权限，并将页码和数据范围交给现有 Mapper 模式处理。

- [ ] **Step 4: 运行单元和接口测试**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaLeaveBalanceServiceImplTest`

Expected: PASS; additionally assert insufficient balance, repeated reserve, repeated confirm and release-after-confirm each return the defined business error or stable result.

- [ ] **Step 5: Commit**

```bash
git add yudao-module-oa
git commit -m "feat(oa): add employee profile and leave balance ledger"
```

### Task 3: 假勤申请与 BPM 终态回写

**Files:**
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/application/OaAttendanceApplicationService.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/application/OaAttendanceApplicationServiceImpl.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/application/listener/OaAttendanceApplicationStatusListener.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/controller/admin/application/**`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/dal/**/OaAttendanceApplication*.java`
- Test: `yudao-module-oa/src/test/java/cn/iocoder/yudao/module/oa/service/application/OaAttendanceApplicationServiceImplTest.java`

**Interfaces:**
- Consumes: `BpmProcessInstanceApi.createProcessInstance(Long, BpmProcessInstanceCreateReqDTO)` and `BpmProcessInstanceStatusEventListener`.
- Produces: `/oa/attendance-application/create`、`/get`、`/page` and process keys `oa_leave`、`oa_overtime`、`oa_business_trip`、`oa_makeup_card`、`oa_outing`.

- [ ] **Step 1: 写流程创建和回写失败测试**

```java
@Test
void createLeave_shouldReserveBalanceAndStoreProcessInstanceId() {
    Long applicationId = applicationService.create(currentUserId, createReqVO);
    OaAttendanceApplicationDO application = applicationMapper.selectById(applicationId);
    assertThat(application.getProcessInstanceId()).isNotBlank();
    assertThat(application.getStatus()).isEqualTo(BpmTaskStatusEnum.RUNNING.getStatus());
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaAttendanceApplicationServiceImplTest`

Expected: FAIL because the application service does not exist.

- [ ] **Step 3: 实现申请、监听器与异常考勤回写**

申请服务先校验时间区间、班次、时间重叠、附件、余额和流程映射，事务中保存申请单、预占余额、调用 BPM、更新 `process_instance_id`。监听器继承 `BpmProcessInstanceStatusEventListener`，按流程定义 Key 过滤；通过时确认余额并更新考勤异常，驳回、撤回、作废时释放预占。监听器以业务单 `id` 作为 BPM `businessKey`，禁止以流程编号反查并修改无关单据。

- [ ] **Step 4: 运行测试并验证异常分支**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaAttendanceApplicationServiceImplTest`

Expected: PASS for process start, terminal approval, rejection, withdrawal, duplicate terminal event, overlap rejection and insufficient balance.

- [ ] **Step 5: Commit**

```bash
git add yudao-module-oa
git commit -m "feat(oa): add attendance applications with BPM callbacks"
```

### Task 4: 假勤前端、OA 工作台与端到端验收

**Files:**
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/oa/attendance/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/oa/employee/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/workbench/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/attendance/**`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/employee/**`
- Test: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/attendance/__tests__/leave-create.spec.ts`

**Interfaces:**
- Consumes: Task 2 and Task 3 REST endpoints; BPM process-detail route.
- Produces: typed `AttendanceApplicationVO` with `id`、`businessType`、`status`、`processInstanceId`、`startTime`、`endTime` and a workbench card deep-linking to BPM.

- [ ] **Step 1: 编写申请表单失败测试**

```ts
it('blocks submission when the end time is not after the start time', async () => {
  await wrapper.find('[data-test="submit"]').trigger('click')
  expect(message.warning).toHaveBeenCalledWith('结束时间必须晚于开始时间')
})
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `pnpm --dir yudao-ui/yudao-ui-admin-vue3 test leave-create.spec.ts`

Expected: FAIL because the component and test configuration do not exist.

- [ ] **Step 3: 实现强类型 API 与页面**

遵循现有 `src/api/bpm/leave/index.ts` 和 `views/bpm/oa/leave` 的请求、列表、详情、流程进度模式；将业务路径改为 `/oa/**`，权限改为 `oa:*`。申请页复用 `ProcessInstanceTimeline` 预测审批节点，禁止 `any`，将候选审批人声明为 `Record<string, number[]>`。工作台只展示数量和深链，待办和已办必须跳转 BPM 原页面。

- [ ] **Step 4: 运行前端检查和人工验收**

Run: `pnpm --dir yudao-ui/yudao-ui-admin-vue3 lint && pnpm --dir yudao-ui/yudao-ui-admin-vue3 typecheck`

Expected: exit code 0.

Manual: 创建请假，确认余额预占、流程可审批、通过后余额扣减；再创建并驳回，确认预占释放；以无权限账号确认菜单、按钮与数据均不可见。

- [ ] **Step 5: Commit**

```bash
git add yudao-ui/yudao-ui-admin-vue3
git commit -m "feat(oa): add attendance workspace and applications"
```

### Task 5: 入转调离与员工状态回写

**Files:**
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/employee/OaEmployeeChangeService.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/employee/OaEmployeeChangeServiceImpl.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/employee/listener/OaEmployeeChangeStatusListener.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/controller/admin/employee/OaEmployeeChangeController.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/dal/dataobject/employee/OaEmployeeChangeDO.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/dal/mysql/employee/OaEmployeeChangeMapper.java`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/employee/change/index.vue`
- Test: `yudao-module-oa/src/test/java/cn/iocoder/yudao/module/oa/service/employee/OaEmployeeChangeServiceImplTest.java`

**Interfaces:**
- Consumes: `AdminUserApi`、`DeptApi` and `BpmProcessInstanceApi`.
- Produces: employee change types `ONBOARD`、`PROBATION`、`TRANSFER`、`PROMOTION`、`RESIGNATION`, each linked to one BPM process instance.

- [ ] **Step 1: 编写离职校验失败测试**

```java
@Test
void completeResignation_shouldRejectOutstandingFirstPhaseWork() {
    assertServiceException(() -> employeeChangeService.complete(resignationId), OA_EMPLOYEE_EXIT_PENDING_WORK);
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaEmployeeChangeServiceImplTest`

Expected: FAIL because employee change service and exit validation do not exist.

- [ ] **Step 3: 实现异动单、流程回写和离职校验**

异动单保存员工、原部门、目标部门、原岗位、目标岗位、生效日期、原因、流程实例和状态。入职通过后创建或激活员工扩展档案；转正、调岗、晋升通过后更新档案并调用 System 的公开服务处理允许更新的用户组织引用；离职通过前校验该员工是否拥有首期未完成任务、待办审批、未结借款与未结束假勤单。将资产交接校验定义为扩展点，由第二期固定资产服务接入，不在首期伪造资产数据。

- [ ] **Step 4: 运行测试**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaEmployeeChangeServiceImplTest`

Expected: PASS for each异动类型的流程发起、通过回写、驳回不更新档案、离职阻塞和离职解除阻塞。

- [ ] **Step 5: Commit**

```bash
git add yudao-module-oa yudao-ui/yudao-ui-admin-vue3 sql/mysql
git commit -m "feat(oa): add employee lifecycle applications"
```
