# 系统管理与登录权限（第一版）

## 当前能力

- 管理员登录：`POST /api/v1/auth/login`
- 当前登录用户：`GET /api/v1/auth/me`
- 用户查询与创建：`GET`、`POST /api/v1/system/users`
- 用户启停：`PUT /api/v1/system/users/{id}/status`
- 角色与权限查询：`GET /api/v1/system/roles`
- 菜单树、部门树、数据字典和操作审计查询
- JWT Bearer Token 鉴权及用户、角色权限校验

## 初始账号

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `admin123` | 平台管理员 |

仅用于本地初步开发。正式环境必须在首次部署时重设管理员密码，并设置 `TX_JWT_SECRET`。

## 本地测试顺序

1. 调用登录接口获取 `accessToken`。
2. 在受保护接口请求头中设置 `Authorization: Bearer {accessToken}`。
3. 查询角色和用户，再创建测试用户或启停用户。

启用 `mysql` 配置档后，用户与角色数据会写入 MySQL；菜单、部门、字典和审计数据仍为当前迭代的内存种子数据。数据库结构见 `src/main/resources/db/schema.sql`。

在 PowerShell 中设置本地数据库密码并启动：

```powershell
$env:SPRING_PROFILES_ACTIVE = 'mysql'
$env:TX_DB_PASSWORD = '你的本地MySQL密码'
mvn "-Dmaven.repo.local=.m2" spring-boot:run
```
