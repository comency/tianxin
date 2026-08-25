# CMS 第一期实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新建可租户隔离的 CMS 内容底座，完成栏目级 BPM 审批和官网/H5、小程序、商城 UniApp 可消费的公开内容 API。

**Architecture:** 新增 `yudao-module-cms`，以内容和不可变版本作为聚合核心；站点、栏目、专题、标签、模板和素材引用为独立资源。栏目通过工作流绑定选择 BPM 定义，内容版本以带前缀的业务键启动流程，CMS 监听 BPM 状态事件并更新版本状态。管理端维护内容与配置，客户端只读取已发布版本。

**Tech Stack:** Java 25、Spring Boot 4.1.0、MyBatis-Plus、MapStruct、Flowable BPM、Vue 3、TypeScript 6、Element Plus、Vite 8、pnpm。

## 全局约束

- 所有新增文件使用 UTF-8；Java、TS、Vue、SQL 和 Markdown 中的中文必须为标准 UTF-8。
- 每个新增 Java 文件顶部添加中文文件说明、作者、创建时间和核心功能；类、接口、枚举、业务方法添加中文文档注释。
- Java 使用 4 个空格缩进、单行不超过 120 字符、Java 25 和 Spring Boot 4.1.0；禁止捕获 `Exception` 作为业务分支。
- Vue/TypeScript 使用 4 个空格缩进、严格类型，不使用 `any`；接口请求沿用 `@/config/axios`。
- CSS 文件首行使用 `@charset "UTF-8";`；HTML 文件使用 `<meta charset="UTF-8">`。
- 不引入新的第三方依赖；复用 `yudao-module-system`、`yudao-module-infra`、`yudao-module-bpm` 和既有前端组件。
- 所有 CMS 表使用 `tenant_id`、`creator`、`create_time`、`updater`、`update_time`、`deleted`，并继承 `BaseDO`。
- 本工作区没有 `.git` 元数据；执行时仅在恢复为 Git 工作树后创建下述提交，不要初始化新仓库或覆盖用户文件。

---

## 目标文件结构

| 路径 | 职责 |
| --- | --- |
| `yudao-module-cms/pom.xml` | CMS 模块依赖。 |
| `yudao-module-cms/src/main/java/.../cms/dal/dataobject/**` | CMS 持久化对象。 |
| `yudao-module-cms/src/main/java/.../cms/dal/mysql/**` | 分页、唯一性和关联查询 Mapper。 |
| `yudao-module-cms/src/main/java/.../cms/service/**` | 站点、栏目、内容、版本、分类资源和公开读取服务。 |
| `yudao-module-cms/src/main/java/.../cms/controller/admin/**` | 管理端配置、内容与审批接口。 |
| `yudao-module-cms/src/main/java/.../cms/controller/app/**` | 仅返回已发布版本的公开接口。 |
| `yudao-module-cms/src/main/java/.../cms/listener/CmsContentProcessStatusListener.java` | 动态流程定义的 BPM 状态事件处理。 |
| `yudao-module-cms/src/test/**` | H2 建表、服务单元测试和 BPM 回调测试。 |
| `sql/mysql/cms-2026-07-25.sql` | 生产建表、字典和第一级菜单权限数据。 |
| `yudao-ui/yudao-ui-admin-vue3/src/api/cms/**` | 严格类型的 CMS 管理端 API。 |
| `yudao-ui/yudao-ui-admin-vue3/src/views/cms/**` | 站点、栏目、内容和审核状态页面。 |

### Task 1: 创建 CMS 模块和可测试的数据库骨架

**Files:**
- Create: `yudao-module-cms/pom.xml`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/enums/ErrorCodeConstants.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/enums/content/CmsContentStatusEnum.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/enums/content/CmsContentVersionStatusEnum.java`
- Create: `yudao-module-cms/src/test/resources/sql/create_tables.sql`
- Create: `yudao-module-cms/src/test/resources/sql/clean.sql`
- Modify: `pom.xml`
- Modify: `yudao-server/pom.xml`

**Interfaces:**
- Produces Maven artifact `cn.iocoder.boot:yudao-module-cms:${revision}`.
- Produces status values: `DRAFT(0)`、`PROCESS(10)`、`APPROVED(20)`、`PUBLISHED(30)`、`ARCHIVED(40)` and version values `DRAFT(0)`、`PROCESS(10)`、`APPROVED(20)`、`REJECTED(30)`、`WITHDRAWN(40)`.

- [ ] **Step 1: 写模块依赖与聚合注册的失败验证**

在 `pom.xml` 的 `<modules>` 中临时引用不存在的 `yudao-module-cms`，执行以下命令，确认 Maven 在模块解析阶段失败：

```powershell
mvn -pl yudao-module-cms -am test -DskipTests
```

Expected: FAIL，错误包含 `Could not find the selected project in the reactor` 或模块目录不存在。

- [ ] **Step 2: 创建最小模块 POM 和枚举**

`yudao-module-cms/pom.xml` 依赖系统、基础设施、BPM、租户、安全、校验、MyBatis 和测试 starter；不要依赖会员和商城模块。枚举采用如下契约：

```java
public enum CmsContentVersionStatusEnum {
    DRAFT(0), PROCESS(10), APPROVED(20), REJECTED(30), WITHDRAWN(40);

    private final Integer status;

    public static boolean isProcess(Integer status) {
        return Objects.equals(PROCESS.status, status);
    }
}
```

给两个状态枚举添加 Lombok `@Getter`，保证后续服务中使用的 `getStatus()` 方法存在。

根 POM 增加 `<module>yudao-module-cms</module>`，`yudao-server/pom.xml` 增加 CMS 依赖，位置紧接 `yudao-module-mp` 之后。

- [ ] **Step 3: 建立测试表与错误码**

在 `create_tables.sql` 创建 `cms_site`、`cms_category`、`cms_workflow_binding`、`cms_content`、`cms_content_version`、`cms_topic`、`cms_tag`、`cms_content_topic`、`cms_content_tag`、`cms_content_asset_ref`、`cms_content_template`。

每张表使用 `BIGINT` 主键、`tenant_id BIGINT NOT NULL DEFAULT 0`，并为下列查询加索引：

```sql
CREATE UNIQUE INDEX uk_cms_site_tenant_code ON cms_site (tenant_id, code, deleted);
CREATE UNIQUE INDEX uk_cms_category_tenant_code ON cms_category (tenant_id, site_id, code, deleted);
CREATE UNIQUE INDEX uk_cms_content_version_no ON cms_content_version (content_id, version_no, deleted);
CREATE INDEX idx_cms_content_public ON cms_content (site_id, category_id, status, deleted);
CREATE INDEX idx_cms_version_process ON cms_content_version (process_instance_id, deleted);
```

`ErrorCodeConstants` 至少定义 `SITE_NOT_EXISTS`、`SITE_CODE_EXISTS`、`CATEGORY_NOT_EXISTS`、`CATEGORY_CODE_EXISTS`、`CATEGORY_CYCLE`、`CONTENT_NOT_EXISTS`、`CONTENT_VERSION_NOT_EXISTS`、`CONTENT_SUBMIT_STATUS_INVALID`、`CONTENT_WORKFLOW_NOT_CONFIGURED`、`CONTENT_VERSION_NOT_APPROVED`、`CONTENT_SLUG_EXISTS`、`TOPIC_CODE_EXISTS`、`TAG_CODE_EXISTS`、`TEMPLATE_CODE_EXISTS` 和 `CONTENT_TEMPLATE_DISABLED`。

- [ ] **Step 4: 执行模块解析和测试数据库初始化**

运行：

```powershell
mvn -pl yudao-module-cms -am test -DskipTests
```

Expected: PASS，Maven 能解析 CMS 模块和其依赖。

- [ ] **Step 5: 提交模块骨架**

在恢复为 Git 工作树时执行：

```powershell
git add pom.xml yudao-server/pom.xml yudao-module-cms
git commit -m "feat(cms): add module skeleton"
```

### Task 2: 实现站点、栏目和工作流绑定配置

**Files:**
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/dataobject/site/CmsSiteDO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/dataobject/category/CmsCategoryDO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/dataobject/workflow/CmsWorkflowBindingDO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/mysql/site/CmsSiteMapper.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/mysql/category/CmsCategoryMapper.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/mysql/workflow/CmsWorkflowBindingMapper.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/site/CmsSiteService.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/site/CmsSiteServiceImpl.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/category/CmsCategoryService.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/category/CmsCategoryServiceImpl.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/site/vo/CmsSiteSaveReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/site/vo/CmsSitePageReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/site/vo/CmsSiteRespVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/category/vo/CmsCategorySaveReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/category/vo/CmsCategoryRespVO.java`
- Create: `yudao-module-cms/src/test/java/cn/iocoder/yudao/module/cms/service/category/CmsCategoryServiceImplTest.java`

**Interfaces:**
- Produces `CmsCategoryService.getCategory(Long id)`、`getCategoryTree(Long siteId)`、`getWorkflowDefinitionKey(Long categoryId, String contentType)`。
- Produces `CmsSiteService.validateSite(Long id)` and `CmsSiteService.getEnabledSite(Long id)`.

- [ ] **Step 1: 编写栏目树和工作流选择的失败单元测试**

在 `CmsCategoryServiceImplTest` 写入以下三类用例：同站点同编码创建失败、父节点改为自身后抛出 `CATEGORY_CYCLE`、栏目和内容类型匹配到启用流程绑定。

```java
@Test
void testUpdateCategory_parentIsSelf() {
    CmsCategoryDO category = randomPojo(CmsCategoryDO.class);
    categoryMapper.insert(category);

    assertServiceException(() -> categoryService.updateCategory(
            new CmsCategorySaveReqVO().setId(category.getId()).setParentId(category.getId())),
            CATEGORY_CYCLE);
}
```

运行：

```powershell
mvn -pl yudao-module-cms -am test -Dtest=CmsCategoryServiceImplTest
```

Expected: FAIL，因为服务、Mapper 和请求 VO 尚未实现。

- [ ] **Step 2: 实现 DO、Mapper 和请求契约**

`CmsSiteDO` 包含 `id`、`code`、`name`、`domain`、`defaultSeoTitle`、`defaultSeoDescription`、`status`；`CmsCategoryDO` 包含 `siteId`、`parentId`、`code`、`name`、`sort`、`status`；`CmsWorkflowBindingDO` 包含 `categoryId`、`contentType`、`processDefinitionKey`、`status`。

Mapper 对外提供下列方法，所有查询自动受租户拦截器约束：

```java
CmsSiteDO selectByCode(String code);
CmsCategoryDO selectBySiteIdAndCode(Long siteId, String code);
List<CmsCategoryDO> selectListBySiteId(Long siteId);
CmsWorkflowBindingDO selectEnabledByCategoryIdAndContentType(Long categoryId, String contentType);
```

- [ ] **Step 3: 实现校验和树构建服务**

`CmsCategoryServiceImpl` 在创建和更新时校验站点存在、编码唯一和父栏目属于同一站点；使用父链循环检查，不允许跨站点挂载。工作流查询必须先按栏目和内容类型匹配，再按栏目和空内容类型匹配；两者均不存在时返回 `null`，由提审服务抛出业务错误。

```java
private void validateParent(Long siteId, Long categoryId, Long parentId) {
    if (parentId == null || Objects.equals(categoryId, parentId)) {
        if (Objects.equals(categoryId, parentId)) {
            throw exception(CATEGORY_CYCLE);
        }
        return;
    }
    CmsCategoryDO parent = validateCategoryExists(parentId);
    if (!Objects.equals(siteId, parent.getSiteId())) {
        throw exception(CATEGORY_NOT_EXISTS);
    }
}
```

- [ ] **Step 4: 运行栏目服务测试**

运行：

```powershell
mvn -pl yudao-module-cms -am test -Dtest=CmsCategoryServiceImplTest
```

Expected: PASS，覆盖唯一性、循环、跨站点父级和工作流选择。

- [ ] **Step 5: 提交站点和栏目配置**

```powershell
git add yudao-module-cms
git commit -m "feat(cms): add site category workflow configuration"
```

### Task 3: 实现内容、不可变版本与分类资源聚合

**Files:**
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/dataobject/content/CmsContentDO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/dataobject/content/CmsContentVersionDO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/dataobject/content/CmsContentTopicDO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/dataobject/content/CmsContentTagDO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/dataobject/content/CmsContentAssetRefDO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/dataobject/topic/CmsTopicDO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/dataobject/tag/CmsTagDO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/dataobject/template/CmsContentTemplateDO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/dataobject/content/CmsContentAssetRefDO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/mysql/content/CmsContentMapper.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/mysql/content/CmsContentVersionMapper.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/content/CmsContentService.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/content/CmsContentServiceImpl.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/content/vo/CmsContentSaveReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/content/vo/CmsContentPageReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/content/vo/CmsContentRespVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/content/vo/CmsContentVersionRespVO.java`
- Create: `yudao-module-cms/src/test/java/cn/iocoder/yudao/module/cms/service/content/CmsContentServiceImplTest.java`

**Interfaces:**
- Produces `Long createContent(CmsContentSaveReqVO reqVO, Long userId)`、`void updateContentDraft(CmsContentSaveReqVO reqVO, Long userId)`、`CmsContentVersionDO getCurrentDraftVersion(Long contentId)`。
- Produces immutable version fields `versionNo`、`slug`、`title`、`summary`、`coverUrl`、`content`、`seoTitle`、`seoDescription`、`status`、`processInstanceId`。

- [ ] **Step 1: 编写内容版本不可覆盖的失败测试**

测试创建内容后版本号为 1；编辑已存在内容会插入版本号 2 而不更新版本号 1；同站点使用相同 `slug` 会抛出 `CONTENT_SLUG_EXISTS`。

```java
@Test
void testUpdateContentDraft_createNewVersion() {
    Long contentId = contentService.createContent(createReqVO(), 1L);
    CmsContentVersionDO first = versionMapper.selectCurrentDraftByContentId(contentId);

    contentService.updateContentDraft(updateReqVO(contentId, "revised-title"), 1L);

    assertEquals(1, versionMapper.selectById(first.getId()).getVersionNo());
    assertEquals(2, versionMapper.selectCurrentDraftByContentId(contentId).getVersionNo());
}
```

运行：

```powershell
mvn -pl yudao-module-cms -am test -Dtest=CmsContentServiceImplTest
```

Expected: FAIL，因为内容聚合尚不存在。

- [ ] **Step 2: 实现内容持久化模型和 Mapper 查询**

`CmsContentDO` 保存稳定 ID、站点、栏目、内容类型、当前草稿版本、当前发布版本和内容状态；`CmsContentVersionDO` 保存上述快照字段与审核状态。版本 Mapper 实现：

```java
CmsContentVersionDO selectCurrentDraftByContentId(Long contentId);
CmsContentVersionDO selectByContentIdAndVersionNo(Long contentId, Integer versionNo);
CmsContentVersionDO selectByProcessInstanceId(String processInstanceId);
CmsContentVersionDO selectPublishedBySiteIdAndSlug(Long siteId, String slug);
```

专题、标签、模板和素材引用采用独立 DO 与关联表。保存内容时替换当前草稿版本的专题、标签和素材引用；已审批版本的关联数据不得修改。

- [ ] **Step 3: 实现内容版本事务和状态校验**

创建内容时依次插入内容主表、版本号 1、关联数据，并回写 `draft_version_id`。编辑必须创建新版本。
内容主表 `status` 表示线上可用性：不存在 `publishedVersionId` 时可写为草稿；存在已发布版本时，编辑和后续审批均保持为已发布。
草稿版本的审核状态仅写在 `CmsContentVersionDO.status`。删除仅允许不存在已发布版本的内容；已发布内容通过归档处理。

```java
@Transactional(rollbackFor = Exception.class)
public void updateContentDraft(CmsContentSaveReqVO reqVO, Long userId) {
    CmsContentDO content = validateContentExists(reqVO.getId());
    validateDraftEditable(content);
    CmsContentVersionDO version = buildNextDraftVersion(content, reqVO, userId);
    contentVersionMapper.insert(version);
    Integer contentStatus = content.getPublishedVersionId() == null
            ? CmsContentStatusEnum.DRAFT.getStatus() : CmsContentStatusEnum.PUBLISHED.getStatus();
    contentMapper.updateById(new CmsContentDO().setId(content.getId())
            .setDraftVersionId(version.getId()).setStatus(contentStatus));
}
```

- [ ] **Step 4: 运行内容聚合测试**

运行：

```powershell
mvn -pl yudao-module-cms -am test -Dtest=CmsContentServiceImplTest
```

Expected: PASS，覆盖版本递增、历史版本不变、Slug 唯一和删除/归档限制。

- [ ] **Step 5: 提交内容聚合**

```powershell
git add yudao-module-cms
git commit -m "feat(cms): add immutable content versions"
```

### Task 4: 实现专题、标签、模板和素材引用管理

**Files:**
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/mysql/topic/CmsTopicMapper.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/mysql/tag/CmsTagMapper.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/mysql/template/CmsContentTemplateMapper.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/mysql/content/CmsContentTopicMapper.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/mysql/content/CmsContentTagMapper.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/dal/mysql/content/CmsContentAssetRefMapper.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/topic/CmsTopicService.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/topic/CmsTopicServiceImpl.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/tag/CmsTagService.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/tag/CmsTagServiceImpl.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/template/CmsContentTemplateService.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/template/CmsContentTemplateServiceImpl.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/content/CmsContentAssetRefService.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/content/CmsContentAssetRefServiceImpl.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/topic/CmsTopicController.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/tag/CmsTagController.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/template/CmsContentTemplateController.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/content/CmsContentAssetRefController.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/topic/vo/CmsTopicSaveReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/topic/vo/CmsTopicPageReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/topic/vo/CmsTopicRespVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/tag/vo/CmsTagSaveReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/tag/vo/CmsTagPageReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/tag/vo/CmsTagRespVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/template/vo/CmsContentTemplateSaveReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/template/vo/CmsContentTemplatePageReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/template/vo/CmsContentTemplateRespVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/content/vo/CmsContentAssetRefRespVO.java`
- Create: `yudao-module-cms/src/test/java/cn/iocoder/yudao/module/cms/service/topic/CmsTopicServiceImplTest.java`
- Create: `yudao-module-cms/src/test/java/cn/iocoder/yudao/module/cms/service/tag/CmsTagServiceImplTest.java`
- Create: `yudao-module-cms/src/test/java/cn/iocoder/yudao/module/cms/service/template/CmsContentTemplateServiceImplTest.java`

**Interfaces:**
- Produces `CmsTopicService.getTopicList(Collection<Long> ids)`、`CmsTagService.getTagList(Collection<Long> ids)` and
  `CmsContentTemplateService.getEnabledTemplate(Long id)`.
- Produces `CmsContentAssetRefMapper.selectListByFileId(Long fileId)` for the later infrastructure-side delete guard.

- [ ] **Step 1: 写资源唯一性和引用替换的失败测试**

为专题、标签、模板分别测试同租户编码唯一；为内容保存测试提交 `[11, 12]` 作为标签并再次保存 `[12, 13]` 后，关联表仅保留
`12` 和 `13`。模板停用后不得被内容表单选择。

```java
assertServiceException(() -> tagService.createTag(new CmsTagSaveReqVO().setCode("news")), TAG_CODE_EXISTS);
assertEquals(Set.of(12L, 13L), contentTagMapper.selectTagIdsByContentVersionId(versionId));
```

- [ ] **Step 2: 实现配置资源 CRUD 与关联替换**

专题、标签和模板全部采用 `code`、`name`、`status`、`sort` 为通用字段；模板追加 `contentType`、`defaultContent`、
`defaultSeoTitle` 和 `defaultSeoDescription`。内容服务内部使用删除旧关联再批量插入新关联的事务操作：

```java
contentTagMapper.deleteByContentVersionId(versionId);
contentTagMapper.insertBatch(tagIds.stream().map(tagId -> new CmsContentTagDO()
        .setContentVersionId(versionId).setTagId(tagId)).toList());
```

素材引用保存 `contentVersionId`、`fileId` 和 `usageType`；内容版本删除前先删除其素材、专题和标签关联。

- [ ] **Step 3: 实现管理端控制器与权限**

三个控制器均提供 `create`、`update`、`delete`、`get`、`page` 和 `list-all-simple`；权限分别为
`cms:topic:*`、`cms:tag:*`、`cms:template:*`。素材引用控制器仅提供查询接口，权限为 `cms:asset:query`。
内容详情接口返回专题、标签、模板和素材编号，表单调用精简列表接口加载选择器。

```java
@GetMapping("/asset-reference/list")
@PreAuthorize("@ss.hasPermission('cms:asset:query')")
public CommonResult<List<CmsContentAssetRefRespVO>> getAssetReferenceList(@RequestParam("fileId") Long fileId) {
    return success(assetRefService.getAssetReferenceList(fileId));
}
```

- [ ] **Step 4: 运行配置资源服务测试**

运行：

```powershell
mvn -pl yudao-module-cms -am test -Dtest=CmsTopicServiceImplTest,CmsTagServiceImplTest,CmsContentTemplateServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected: PASS，覆盖编码唯一、启停校验、关联替换和素材引用查询。

- [ ] **Step 5: 提交内容配置资源**

```powershell
git add yudao-module-cms
git commit -m "feat(cms): add content taxonomy and templates"
```

### Task 5: 接入动态栏目 BPM 审批与回调幂等

**Files:**
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/listener/CmsContentProcessStatusListener.java`
- Modify: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/content/CmsContentService.java`
- Modify: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/content/CmsContentServiceImpl.java`
- Modify: `yudao-module-cms/src/test/java/cn/iocoder/yudao/module/cms/service/content/CmsContentServiceImplTest.java`

**Interfaces:**
- Consumes `BpmProcessInstanceApi.createProcessInstance(Long, BpmProcessInstanceCreateReqDTO)` and `BpmProcessInstanceStatusEvent`.
- Produces `void submitContent(Long contentId, Long userId)` and `void updateContentVersionAuditStatus(String processInstanceId, Integer bpmStatus)`.
- Business key format is exactly `cms-content-version:{versionId}`.

- [ ] **Step 1: 写提审与重复 BPM 回调的失败测试**

Mock `BpmProcessInstanceApi`。断言提审使用当前草稿版本、查询栏目流程绑定并将版本置为 `PROCESS`；对同一个已结束流程再次调用状态更新不新增版本、不改变状态。

```java
verify(bpmProcessInstanceApi).createProcessInstance(eq(1L), argThat(req ->
        req.getBusinessKey().equals("cms-content-version:" + versionId)
                && req.getProcessDefinitionKey().equals("cms-article-review")));
```

运行：

```powershell
mvn -pl yudao-module-cms -am test -Dtest=CmsContentServiceImplTest
```

Expected: FAIL，因为提交和回调方法尚未实现。

- [ ] **Step 2: 实现提审事务**

仅允许当前草稿版本为 `DRAFT` 且内容主状态为 `DRAFT` 或 `PUBLISHED` 时提审。获取
`categoryService.getWorkflowDefinitionKey(categoryId, contentType)`；为空则抛出 `CONTENT_WORKFLOW_NOT_CONFIGURED`。
调用 BPM 成功后写入版本 `processInstanceId` 和版本状态 `PROCESS`；只有尚无线上版本的内容主状态才更新为 `PROCESS`。

```java
String processInstanceId = bpmProcessInstanceApi.createProcessInstance(userId,
        new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(definitionKey)
                .setBusinessKey("cms-content-version:" + version.getId()));
contentVersionMapper.updateById(new CmsContentVersionDO().setId(version.getId())
        .setProcessInstanceId(processInstanceId).setStatus(CmsContentVersionStatusEnum.PROCESS.getStatus()));
if (content.getPublishedVersionId() == null) {
    contentMapper.updateById(new CmsContentDO().setId(content.getId())
            .setStatus(CmsContentStatusEnum.PROCESS.getStatus()));
}
```

- [ ] **Step 3: 实现动态流程事件监听器**

不要继承 `BpmProcessInstanceStatusEventListener`，因为其仅支持固定流程定义 Key。实现 `ApplicationListener<BpmProcessInstanceStatusEvent>`，仅处理以 `cms-content-version:` 开头的业务键，按 `event.getId()` 查版本并交给内容服务处理。

```java
public void onApplicationEvent(BpmProcessInstanceStatusEvent event) {
    if (!StrUtil.startWith(event.getBusinessKey(), "cms-content-version:")) {
        return;
    }
    contentService.updateContentVersionAuditStatus(event.getId(), event.getStatus());
}
```

通过 BPM 状态转换将通过写为 `APPROVED`，驳回写为 `REJECTED`；仅当版本仍是 `PROCESS` 时更新，其他状态记录 warn 并返回，实现回调幂等。

- [ ] **Step 4: 运行 BPM 服务测试**

运行：

```powershell
mvn -pl yudao-module-cms -am test -Dtest=CmsContentServiceImplTest
```

Expected: PASS，覆盖无流程、重复提审、审批通过、审批驳回和重复回调。

- [ ] **Step 5: 提交审批闭环**

```powershell
git add yudao-module-cms
git commit -m "feat(cms): integrate category BPM approval"
```

### Task 6: 提供管理端和公开内容 REST API

**Files:**
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/site/CmsSiteController.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/category/CmsCategoryController.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/content/CmsContentController.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/content/vo/CmsContentSubmitReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/admin/content/vo/CmsContentPublishReqVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/app/content/AppCmsContentController.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/controller/app/content/vo/AppCmsContentRespVO.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/convert/site/CmsSiteConvert.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/convert/category/CmsCategoryConvert.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/convert/content/CmsContentConvert.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/content/CmsPublicContentService.java`
- Create: `yudao-module-cms/src/main/java/cn/iocoder/yudao/module/cms/service/content/CmsPublicContentServiceImpl.java`
- Create: `yudao-module-cms/src/test/java/cn/iocoder/yudao/module/cms/service/content/CmsPublicContentServiceTest.java`

**Interfaces:**
- Admin endpoints: `/cms/site/*`、`/cms/category/*`、`/cms/content/create`、`/cms/content/update`、`/cms/content/submit`、`/cms/content/publish`、`/cms/content/page`、`/cms/content/version-list`。
- Public endpoints: `GET /app-api/cms/content/list?siteCode={siteCode}&categoryCode={categoryCode}` and `GET /app-api/cms/content/get-by-slug?siteCode={siteCode}&slug={slug}`.
- Produces `CmsPublicContentService.getBySlug(String siteCode, String slug)` and
  `CmsPublicContentService.getPage(String siteCode, String categoryCode, Integer pageNo, Integer pageSize)`.

- [ ] **Step 1: 写公开接口不泄露草稿的失败测试**

插入同内容的已发布版本和新草稿版本，调用公开服务，断言只返回内容主表 `publishedVersionId` 指向的版本；草稿和审批中内容返回空列表或 `CONTENT_NOT_EXISTS`。

```java
assertEquals("published-title", publicContentService.getBySlug("portal", "news-1").getTitle());
assertNull(publicContentService.getBySlug("portal", "draft-only"));
```

- [ ] **Step 2: 实现 VO、MapStruct 和控制器权限**

管理控制器采用 `CommonResult`、`PageResult`、`@Validated`、`@Operation` 和 `@PreAuthorize`，权限严格使用：

```java
@PreAuthorize("@ss.hasPermission('cms:content:create')")
@PreAuthorize("@ss.hasPermission('cms:content:update')")
@PreAuthorize("@ss.hasPermission('cms:content:submit')")
@PreAuthorize("@ss.hasPermission('cms:content:query')")
```

公开控制器使用 `@PermitAll`，响应 VO 不包含 `processInstanceId`、审核状态、编辑者、内部素材引用或未发布 SEO 草稿。

- [ ] **Step 3: 实现公开读取和发布版本切换服务**

增加 `publishApprovedVersion(Long contentId, Long versionId)` 作为第一期内部服务方法，仅接受 `APPROVED` 版本，原子回写 `publishedVersionId`、`status=PUBLISHED`。内容分页响应额外从 `draftVersionId` 关联填充 `draftVersionStatus`，供前端确定是否可提审。公开查询用 `content.publishedVersionId = version.id` 联表读取，过滤站点与栏目启用状态。

```java
public void publishApprovedVersion(Long contentId, Long versionId) {
    CmsContentVersionDO version = validateVersion(contentId, versionId);
    if (!CmsContentVersionStatusEnum.APPROVED.getStatus().equals(version.getStatus())) {
        throw exception(CONTENT_VERSION_NOT_APPROVED);
    }
    contentMapper.updateById(new CmsContentDO().setId(contentId).setPublishedVersionId(versionId)
            .setStatus(CmsContentStatusEnum.PUBLISHED.getStatus()));
}
```

- [ ] **Step 4: 运行管理和公开服务测试**

运行：

```powershell
mvn -pl yudao-module-cms -am test -Dtest=CmsContentServiceImplTest,CmsPublicContentServiceTest
```

Expected: PASS，审批通过版本可发布，公开接口只读线上版本。

- [ ] **Step 5: 提交 REST API**

```powershell
git add yudao-module-cms
git commit -m "feat(cms): expose admin and public content APIs"
```

### Task 7: 实现 Vue 管理端站点、栏目、内容和审核视图

**Files:**
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/cms/site/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/cms/category/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/cms/content/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/cms/topic/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/cms/tag/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/cms/template/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/api/cms/asset/index.ts`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/site/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/site/SiteForm.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/category/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/category/CategoryForm.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/topic/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/topic/TopicForm.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/tag/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/tag/TagForm.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/template/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/template/TemplateForm.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/asset/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/content/index.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/content/ContentForm.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/content/VersionDrawer.vue`
- Create: `yudao-ui/yudao-ui-admin-vue3/src/views/cms/content/AuditDrawer.vue`

**Interfaces:**
- Consumes the Task 6 endpoints and types `CmsSiteVO`、`CmsCategoryVO`、`CmsContentVO`、`CmsContentVersionVO`.
- Produces no new backend contracts; all actions respect `v-hasPermi` permissions.

- [ ] **Step 1: 创建严格类型 API 定义并运行类型检查失败验证**

每个 API 文件导出数据结构和请求函数；禁止 `any`。例如：

```ts
export interface CmsContentVO {
    id: number
    siteId: number
    categoryId: number
    title: string
    status: number
    draftVersionStatus: number
    draftVersionId: number | null
    publishedVersionId: number | null
}

export const submitContent = (id: number): Promise<void> =>
    request.post({ url: '/cms/content/submit', data: { id } })
```

运行：

```powershell
pnpm --dir yudao-ui/yudao-ui-admin-vue3 ts:check
```

Expected: FAIL，直到视图组件和导入完整。

- [ ] **Step 2: 实现站点和栏目维护页面**

站点列表支持名称、编码、状态筛选和新增编辑；栏目页按站点加载树，表单限制父节点为同站点非自身节点，配置内容类型和 BPM 流程定义 Key。专题、标签和模板页面按同一列表加弹窗表单模式实现，模板页额外编辑默认内容和 SEO。素材引用页按文件编号查询引用它的内容版本、标题和用途，只读展示。按钮分别绑定 `cms:site:create`、`cms:site:update`、`cms:category:create`、`cms:category:update`、`cms:workflow:update`、`cms:topic:*`、`cms:tag:*`、`cms:template:*`、`cms:asset:query`。

- [ ] **Step 3: 实现内容和版本操作页面**

内容列表展示站点、栏目、标题、当前草稿、线上版本、审核状态和更新时间。`ContentForm.vue` 编辑标题、摘要、封面、正文、Slug 和 SEO；保存调用创建或更新，提审操作先保存成功后调用 `submitContent`。`VersionDrawer.vue` 只读显示历史版本；`AuditDrawer.vue` 显示流程实例编号和状态，并跳转现有 `/bpm/task/todo` 或 `/bpm/process-instance` 页面。

```vue
<el-button v-hasPermi="['cms:content:submit']" :disabled="row.draftVersionStatus !== VERSION_STATUS.DRAFT"
           @click="handleSubmit(row.id)">
    提交审批
</el-button>
```

- [ ] **Step 4: 运行前端静态验证**

运行：

```powershell
pnpm --dir yudao-ui/yudao-ui-admin-vue3 ts:check
pnpm --dir yudao-ui/yudao-ui-admin-vue3 lint:eslint:check
```

Expected: PASS，没有 TypeScript 隐式 `any`、未使用导入或 Vue 模板类型错误。

- [ ] **Step 5: 提交管理端页面**

```powershell
git add yudao-ui/yudao-ui-admin-vue3/src/api/cms yudao-ui/yudao-ui-admin-vue3/src/views/cms
git commit -m "feat(cms): add content management views"
```

### Task 8: 编写部署 SQL、菜单权限和端到端验证清单

**Files:**
- Create: `sql/mysql/cms-2026-07-25.sql`
- Modify: `docs/README.md`
- Modify: `docs/00-使用前准备与通用约定.md`

**Interfaces:**
- Produces MySQL 8 可重复执行的 CMS 建表、索引、字典和菜单脚本。
- Produces CMS 文档入口与“内容创建 -> BPM 审批 -> 发布版本 -> 公开读取”操作闭环。

- [ ] **Step 1: 写 SQL 幂等性验证脚本**

为 MySQL 建表使用 `CREATE TABLE IF NOT EXISTS`；对菜单与按钮使用固定 CMS 菜单 ID 且采用 `INSERT ... ON DUPLICATE KEY UPDATE`。在执行前先在目标库检索是否占用这些 ID：

```sql
SELECT id, name FROM system_menu WHERE id BETWEEN 7600 AND 7699;
```

Expected: 无非 CMS 菜单；若存在冲突，先在 SQL 文件中整体替换为未使用连续 ID 后再执行。

- [ ] **Step 2: 实现生产表与权限菜单 SQL**

脚本创建 Task 1 的全部表及索引，字符集使用 `utf8mb4`、排序规则使用 `utf8mb4_unicode_ci`。创建一级菜单 `CMS 内容管理`，第一期创建工作台、站点、栏目、专题、标签、内容模板、素材引用、内容管理、内容审核、审批流程绑定及每页的查询、新增、修改、删除、提审、归档、发布按钮权限。

菜单组件路径必须与 Task 7 一致，例如：

```sql
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component,
                         component_name, status, visible, keep_alive, always_show, creator)
VALUES (7603, '内容管理', 'cms:content:query', 2, 3, 7600, 'content', 'ep:document',
        'cms/content/index', 'CmsContent', 0, '1', '1', '1', 'admin')
ON DUPLICATE KEY UPDATE name = VALUES(name), permission = VALUES(permission),
    component = VALUES(component), component_name = VALUES(component_name), deleted = '0';
```

- [ ] **Step 3: 补充运行说明和人工验收流程**

在 `docs/README.md` 增加 CMS 文档入口；在通用操作手册增加 CMS 章节链接与 UTF-8 内容要求。人工验收按以下步骤执行：创建站点和栏目，绑定已发布 BPM 流程，创建内容并提审，在 BPM 待办通过，调用发布动作，使用 `/app-api/cms/content/get-by-slug` 验证只返回已发布版本，再编辑形成修订版本并确认公开内容不变。

- [ ] **Step 4: 执行完整验证**

运行：

```powershell
mvn -pl yudao-module-cms -am test
pnpm --dir yudao-ui/yudao-ui-admin-vue3 ts:check
pnpm --dir yudao-ui/yudao-ui-admin-vue3 lint
```

Expected: 三个命令均以 exit code 0 完成。随后在 MySQL 测试库连续执行两次 `sql/mysql/cms-2026-07-25.sql`，第二次不产生重复菜单、重复索引或错误。

- [ ] **Step 5: 提交部署脚本和文档**

```powershell
git add sql/mysql/cms-2026-07-25.sql docs
git commit -m "docs(cms): add phase one deployment guide"
```

## 计划自检

- 规格覆盖：Task 1 建立模块、租户表和错误码；Task 2 建立站点、栏目和工作流绑定；Task 3 实现内容和版本；Task 4 实现专题、标签、模板和素材引用；Task 5 实现 BPM；Task 6 提供管理和公开 API；Task 7 提供管理端；Task 8 提供菜单、文档和端到端验证。
- 范围边界：发布计划、定时任务、公众号发布、会员人群、App、邮件、短信和统计分析属于已确认的第二至四期，不在本计划编码范围内。
- 类型一致性：所有 BPM 业务键均为 `cms-content-version:{versionId}`；公开 API 只依赖 `publishedVersionId`；前端与后端均使用 `CmsContentVO`、`CmsContentVersionVO` 对应字段。
- 无占位符：计划不包含未决实现项；外部渠道事项明确放入后续已命名阶段。
