# OA 行政协同与审批 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 交付公告范围、通讯录、日程、会议、任务和统一 OA 审批入口，并保持 BPM 为审批事实来源。

**Architecture:** OA 协同实体独立持久化；公告正文复用 System 公告能力，成员和部门引用 System，联系人跳转 IM。会议纪要只能创建任务，任务永不自动关闭；审批中心只聚合并深链 BPM。

**Tech Stack:** Java 25、Spring Boot、MyBatis-Plus、Vue 3、TypeScript、Element Plus、BPM、IM。

## Global Constraints

- 所有文本和源文件为 UTF-8，4 空格缩进，Java 业务类和方法使用完整中文注释。
- 日程和会议必须做时间区间冲突校验；任务逾期只提醒，不自动关闭。
- 公告、审批、用户和消息仍由既有 System、BPM、IM 持有主事实。
- 当前目录无 Git 仓库；恢复 Git 后按任务提交。

---

### Task 1: 行政协同领域与会议冲突校验

**Files:**
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/collaboration/**`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/controller/admin/collaboration/**`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/dal/**/OaCalendar*.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/dal/**/OaMeeting*.java`
- Test: `yudao-module-oa/src/test/java/cn/iocoder/yudao/module/oa/service/collaboration/OaMeetingServiceImplTest.java`

**Interfaces:**
- Produces: `createMeeting(OaMeetingCreateReqVO)` and `hasConflict(roomId, startTime, endTime, excludedMeetingId)`.

- [ ] **Step 1: 编写会议冲突失败测试**

```java
@Test
void createMeeting_shouldRejectOverlappingRoomReservation() {
    assertServiceException(() -> meetingService.createMeeting(overlapReqVO), OA_MEETING_ROOM_TIME_CONFLICT);
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaMeetingServiceImplTest`

Expected: FAIL because meeting service and error code do not exist.

- [ ] **Step 3: 实现会议、日程和通讯录服务**

建模会议室、会议、参会人、个人/部门日程和会议纪要；通过 `start_time < requested_end AND end_time > requested_start` 查询冲突，并用数据库事务防止同一会议室并发预约。通讯录只读取 System 用户、部门和岗位，页面提供 IM 路由，不创建联系人副本。公告发布范围和已读回执扩展为 OA 辅助表，不复制 System 公告正文。

- [ ] **Step 4: 运行测试**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaMeetingServiceImplTest`

Expected: PASS for new booking, boundary non-conflict, room conflict, attendee conflict and cancelled meeting reuse.

- [ ] **Step 5: Commit**

```bash
git add yudao-module-oa sql/mysql
git commit -m "feat(oa): add calendar meeting and directory services"
```

### Task 2: 任务、会议纪要与提醒

**Files:**
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/task/**`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/job/OaTaskReminderJob.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/controller/admin/task/**`
- Test: `yudao-module-oa/src/test/java/cn/iocoder/yudao/module/oa/service/task/OaTaskServiceImplTest.java`

**Interfaces:**
- Consumes: `NotifyMessageSendApi` for notifications.
- Produces: task states `NOT_STARTED`、`IN_PROGRESS`、`COMPLETED`、`CLOSED`、`OVERDUE` and `createFromMeetingMinutes(meetingId, tasks)`.

- [ ] **Step 1: 写状态与提醒失败测试**

```java
@Test
void remindOverdueTasks_shouldNotifyAssigneeWithoutClosingTask() {
    reminderJob.executeInternal();
    assertThat(taskMapper.selectById(taskId).getStatus()).isEqualTo(OVERDUE.getStatus());
    verify(notifyMessageSendApi).sendSingleMessageToAdmin(any(NotifySendSingleToUserReqDTO.class));
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaTaskServiceImplTest`

Expected: FAIL because task service and scheduled job do not exist.

- [ ] **Step 3: 实现任务状态机和定时提醒**

只允许 `未开始 -> 进行中 -> 已完成 -> 已关闭` 的正向转换，截止时间已过且未关闭时标记逾期；逾期任务继续可被完成或关闭。会议纪要创建任务时验证会议创建人或组织者权限。定时任务按任务和提醒日期去重，以任务标识和提醒类型作为幂等键，通知负责人和配置的上级，不更新为已关闭。

- [ ] **Step 4: 运行测试**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaTaskServiceImplTest`

Expected: PASS for legal and illegal transitions, meeting-task creation, repeated job execution and overdue completion.

- [ ] **Step 5: Commit**

```bash
git add yudao-module-oa
git commit -m "feat(oa): add collaborative tasks and overdue reminders"
```

### Task 3: 协同前端与审批中心聚合

**Files:**
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/oa/collaboration/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/oa/task/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/notice/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/directory/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/calendar/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/meeting/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/task/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/application/index.vue`
- Test: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/meeting/__tests__/meeting-form.spec.ts`

**Interfaces:**
- Consumes: `/oa/meeting/**`、`/oa/task/**` and existing BPM routes.
- Produces: OA approval page containing only `发起申请` and deep links to BPM `待办任务`、`已办任务`、`抄送我的`、`我的流程`.

- [ ] **Step 1: 写会议冲突展示失败测试**

```ts
it('shows the server conflict message and keeps entered fields', async () => {
  mockedCreateMeeting.mockRejectedValue(createHttpError('会议室在该时间段已被占用'))
  await submit()
  expect(wrapper.find('[data-test="subject"]').element.value).toBe('周例会')
})
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `pnpm --dir yudao-ui/yudao-ui-admin-vue3 test meeting-form.spec.ts`

Expected: FAIL because meeting page does not exist.

- [ ] **Step 3: 实现页面与深链**

按现有列表、`ContentWrap`、`Pagination` 与 `v-hasPermi` 模式实现，所有 API VO 显式定义 TypeScript 类型。公告页展示范围和已读状态，通讯录只显示 System 用户和部门信息并提供 IM 跳转。会议预约表单在客户端先校验起止时间，服务端错误保留表单；任务页区分负责人、协作人和创建人可见范围。审批中心不得复制 BPM 任务列表数据，使用路由跳转和工作台数量卡片。

- [ ] **Step 4: 验证前端质量与场景**

Run: `pnpm --dir yudao-ui/yudao-ui-admin-vue3 lint && pnpm --dir yudao-ui/yudao-ui-admin-vue3 typecheck`

Expected: exit code 0.

Manual: 预约冲突会议、创建纪要任务、触发逾期提醒、从工作台进入 BPM 待办；使用非参会人员和非负责人账号验证数据范围。

- [ ] **Step 5: Commit**

```bash
git add yudao-ui/yudao-ui-admin-vue3
git commit -m "feat(oa): add collaboration views and approval links"
```

### Task 4: 通用行政审批入口与流程映射配置

**Files:**
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/application/OaGenericApplicationService.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/service/application/OaProcessMappingService.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/controller/admin/application/OaGenericApplicationController.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/controller/admin/setting/OaProcessMappingController.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/dal/dataobject/application/OaGenericApplicationDO.java`
- Create: `yudao-module-oa/src/main/java/cn/iocoder/yudao/module/oa/dal/dataobject/setting/OaProcessMappingDO.java`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/application/generic-create.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/oa/setting/process-mapping/index.vue`
- Test: `yudao-module-oa/src/test/java/cn/iocoder/yudao/module/oa/service/application/OaGenericApplicationServiceImplTest.java`

**Interfaces:**
- Consumes: `BpmProcessInstanceApi` and configured process mapping by `businessType`.
- Produces: generic business types `CAR_USE`、`SEAL_USE`、`CONTRACT_REQUEST`; each stores a JSON payload for its temporary application form and a BPM `processInstanceId`.

- [ ] **Step 1: 编写流程映射缺失失败测试**

```java
@Test
void createGenericApplication_shouldRejectMissingProcessMapping() {
    assertServiceException(() -> applicationService.create(CAR_USE, request), OA_PROCESS_MAPPING_NOT_EXISTS);
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaGenericApplicationServiceImplTest`

Expected: FAIL because generic application and process mapping services do not exist.

- [ ] **Step 3: 实现受限的通用申请和流程映射**

只允许 `CAR_USE`、`SEAL_USE`、`CONTRACT_REQUEST` 三种首期通用类型，拒绝其他类型。流程映射必须唯一关联业务类型、流程定义 Key、启用状态和表单版本；创建申请时校验映射、附件和 JSON Schema，再保存单据并启动 BPM。终态监听器只回写申请状态。不得在该任务创建车辆派车、印章借还或合同履约台账，这些属于第二期。

- [ ] **Step 4: 运行测试和配置验收**

Run: `mvn -pl yudao-module-oa -am test -Dtest=OaGenericApplicationServiceImplTest`

Expected: PASS for missing mapping, disabled mapping, allowed type, disallowed type, BPM approval and duplicate terminal event.

Manual: 配置三个流程定义 Key，分别发起用车、用印和合同申请，确认业务详情可追踪 BPM 流程，但菜单中不存在第二期的派车、印章借还和合同履约页面。

- [ ] **Step 5: Commit**

```bash
git add yudao-module-oa yudao-ui/yudao-ui-admin-vue3 sql/mysql
git commit -m "feat(oa): add generic administrative approval entry"
```
