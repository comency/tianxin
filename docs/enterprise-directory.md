# 企业名录首批数据

已导入 24 家企业，分为四类：

| 分类 | 数量 |
| --- | ---: |
| 原材料供应商 | 7 |
| 生产制造企业 | 6 |
| 施工安装企业 | 5 |
| 工程甲方 | 6 |

启用 MySQL 配置档后，使用以下受保护接口查询：

- `GET /api/v1/enterprises`
- `GET /api/v1/enterprises?category=UPSTREAM`
- `GET /api/v1/enterprises/categories`

请求头需要带管理员登录后取得的 Bearer Token。
