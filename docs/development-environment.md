# 开发环境准备

## 已确认的项目基线

- Java：JDK 25（项目决策，覆盖开发方案文档中的 Java 21）
- Maven：3.9.x
- Node.js：22.x
- MySQL：8.0
- Redis：7.x
- 后端：Spring Boot 3.x、Spring Cloud 2024.x / Spring Cloud Alibaba
- 前端：Vue 3、TypeScript、Vite、Element Plus、Pinia

## 当前机器状态

- Microsoft OpenJDK 25.0.4.1 已安装，`JAVA_HOME=C:\Program Files\Java\jdk-25`
- Maven 3.9.16 已安装
- Node.js 22.15.0、npm 10.9.2 已安装
- MySQL 8.0 Windows 服务已安装
- Redis 7.4.9 安装在 `.local/redis`，数据保存在 `.local/redis-data`

## Redis 使用

项目根目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-redis.ps1
```

停止 Redis：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\stop-redis.ps1
```

默认连接：`127.0.0.1:6379`。此安装用于本地开发；生产环境应按方案使用 Linux、Docker 或 Kubernetes 部署 Redis。

## 后续中间件

方案还要求 Nacos、RabbitMQ、Elasticsearch 8.x、MinIO、XXL-Job、Prometheus、Grafana 和 SkyWalking。建议项目骨架确定后，以 Docker Compose 统一固定版本和配置。目前本机未安装 Docker，且仓库尚无代码提交。
