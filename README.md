# 茶叶品类与冲泡参数记录本

为茶文化爱好者打造的个人茶叶管理系统，登记茶叶基础信息、储存状态，记录水温、投茶量、出汤时长等冲泡参数，搭建个人专属品茶数据库。

## 功能特性

### 🍵 茶叶档案管理
- 录入茶品名称、茶类、产区、采摘年份
- 储存方式与现有存量登记
- 茶类与产区预置，分类筛选浏览
- 关键词搜索功能

### ☕ 冲泡参数设置
- 为单款茶叶定制多套冲泡方案
- 水温、投茶量、茶水比、注水量配置
- 逐泡出汤时长设置（首泡 / 二泡 / 三泡 / 后续）
- 快速填充茶类推荐参数模板
- 默认参数标记

### 📦 仓储状态更新
- 定期记录储存环境（温度、湿度）
- 密封状况追踪
- 存量变动记录与自动更新
- 历史变动日志

### 📝 品饮体感备注
- 多维度评分（香气、汤色、口感、回甘）
- 各维度文字描述记录
- 综合评分
- 整体感受与心得

### 📊 数据对比视图
- 多款茶叶基础信息对比
- 冲泡参数可视化对比
- 品饮评分雷达式对比
- 出汤时长气泡图对比

## 技术栈

### 前端
- **框架**：Vue 3 + Vite 5
- **UI 组件库**：Element Plus
- **路由**：Vue Router 4
- **HTTP 客户端**：Axios

### 后端
- **框架**：Spring Boot 3.3
- **JDK**：17
- **ORM**：Spring Data JPA
- **数据库迁移**：Flyway
- **缓存**：Redis（String 类型缓存茶类参数模板）
- **数据库**：MySQL 8.0
- **构建工具**：Maven

### 部署
- **容器化**：Docker + Docker Compose
- **Web 服务器**：Nginx
- **分层缓存**：Docker 原生分层构建缓存

## 快速开始

### 环境要求
- Docker 20.10+
- Docker Compose v2+
- 可用端口：18041、19041、13341、16341

### 一键启动

```bash
chmod +x start.sh
./start.sh
```

### 手动启动

```bash
# 构建并启动所有服务
docker compose up -d --build

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

### 访问地址

- **前端页面**：http://localhost:18041
- **后端 API**：http://localhost:19041/api
- **MySQL**：127.0.0.1:13341
- **Redis**：127.0.0.1:16341

### 停止服务

```bash
docker compose down

# 停止并清除数据卷（谨慎使用）
docker compose down -v
```

## 端口配置

| 服务 | 宿主机端口 | 容器内端口 | 说明 |
|------|-----------|-----------|------|
| 前端 | 18041 | 80 | Nginx 静态资源 |
| 后端 | 19041 | 8080 | Spring Boot API |
| MySQL | 13341 | 3306 | 关系型数据库 |
| Redis | 16341 | 6379 | 缓存服务 |

> 所有端口均绑定 `127.0.0.1`，仅本机可访问。如需修改端口，编辑 `.env` 文件。

## 项目结构

```
.
├── .env                      # 全局环境变量配置
├── docker-compose.yml        # Docker Compose 编排文件
├── start.sh                  # 一键启动脚本
├── README.md                 # 项目说明文档
├── backend/                  # 后端 Spring Boot 项目
│   ├── Dockerfile            # 后端 Docker 构建文件
│   ├── pom.xml               # Maven 配置
│   ├── settings.xml          # Maven 阿里云镜像配置
│   └── src/main/java/com/tea/tracker/
│       ├── TeaTrackerApplication.java
│       ├── config/           # 配置类（Redis、CORS）
│       ├── controller/       # REST API 控制器
│       ├── dto/              # 数据传输对象
│       ├── entity/           # JPA 实体
│       ├── exception/        # 全局异常处理
│       ├── init/             # 启动初始化
│       ├── repository/       # JPA Repository
│       └── service/          # 业务逻辑 + Redis 缓存
└── frontend/                 # 前端 Vue 3 项目
    ├── Dockerfile            # 前端 Docker 构建文件
    ├── nginx.conf            # Nginx 配置
    ├── vite.config.js        # Vite 配置
    ├── package.json          # npm 依赖配置
    └── src/
        ├── api/              # API 接口封装
        ├── router/           # 路由配置
        ├── utils/            # 工具函数与常量
        └── views/            # 页面组件
```

## 数据库配置

### MySQL 连接信息
- **用户名**：tea_user
- **密码**：tea_password2024
- **数据库名**：tea_tracker
- **Root 密码**：teatracker2024

### 数据持久化
MySQL 和 Redis 数据均通过 Docker Volume 持久化，容器删除后数据不会丢失。

## Redis 缓存说明

系统启动时自动将 8 大茶类的冲泡参数模板缓存到 Redis：

- 绿茶、红茶、乌龙茶、普洱茶
- 白茶、黑茶、黄茶、花茶

缓存内容包括推荐水温、投茶量、茶水比、出汤时长等，降低数据库访问压力。缓存有效期 24 小时。

## Docker 构建缓存机制

项目采用 Docker 原生分层缓存，无需额外语法。依赖层与源码层彻底分离，仅修改业务代码时不触发依赖重新下载。

### 构建上下文控制
前后端均配置 `.dockerignore`，排除以下无关文件传入构建上下文：
- 依赖目录：`node_modules`、`target`、`dist`
- IDE 配置：`.idea`、`.vscode`、`*.iml`
- 日志与临时文件：`*.log`、`.DS_Store`、`.env`
- 缓存目录：`.npm-cache`、`.cache`

### 前端构建分层
1. 基础镜像（`node:18-alpine`）
2. 复制 `.npmrc`（中科大镜像源配置）
3. 复制 `package.json` + `package-lock.json`
4. 安装依赖（`npm ci`，严格按锁文件安装）
5. 复制全部源码
6. 构建产物（`npm run build`）
7. Nginx 阶段部署静态资源

### 后端构建分层
1. 基础镜像（`maven:3.9-eclipse-temurin-17`）
2. 复制 `settings.xml`（阿里云镜像配置）
3. 复制 `pom.xml`
4. 下载依赖（`mvn dependency:go-offline`）
5. 复制源码（`src` 目录）
6. 编译打包（`mvn package -DskipTests`）
7. JRE 阶段部署 jar 包

> **缓存效果**：仅修改业务代码时，依赖下载层完全复用，构建速度大幅提升。

## 国内镜像源配置

### npm 镜像
使用中科大 npm 镜像：`https://registry.npmmirror.com`

### Maven 镜像
使用阿里云公共仓库：`https://maven.aliyun.com/repository/public`

### Docker 镜像
所有基础镜像统一通过 `DOCKER_REGISTRY` 环境变量前缀引用，可在 `.env` 中配置国内镜像加速器。

## API 接口

### 茶叶档案
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/teas` | 获取茶叶列表（支持 category/region/keyword 筛选） |
| GET | `/api/teas/{id}` | 获取茶叶详情 |
| POST | `/api/teas` | 创建茶叶档案 |
| PUT | `/api/teas/{id}` | 更新茶叶档案 |
| DELETE | `/api/teas/{id}` | 删除茶叶档案 |
| GET | `/api/teas/categories` | 获取所有茶类 |
| GET | `/api/teas/regions` | 获取所有产区 |

### 冲泡参数
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/teas/{teaId}/brewing-params` | 获取茶叶的冲泡参数列表 |
| GET | `/api/teas/{teaId}/brewing-params/{id}` | 获取单条冲泡参数 |
| POST | `/api/teas/{teaId}/brewing-params` | 添加冲泡参数 |
| PUT | `/api/teas/{teaId}/brewing-params/{id}` | 更新冲泡参数 |
| DELETE | `/api/teas/{teaId}/brewing-params/{id}` | 删除冲泡参数 |
| GET | `/api/teas/{teaId}/brewing-params/template` | 获取茶类推荐模板 |

### 仓储记录
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/teas/{teaId}/storage-records` | 获取仓储记录列表 |
| POST | `/api/teas/{teaId}/storage-records` | 添加仓储记录 |
| PUT | `/api/teas/{teaId}/storage-records/{id}` | 更新仓储记录 |
| DELETE | `/api/teas/{teaId}/storage-records/{id}` | 删除仓储记录 |

### 品饮记录
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/teas/{teaId}/tasting-notes` | 获取品饮记录列表 |
| POST | `/api/teas/{teaId}/tasting-notes` | 添加品饮记录 |
| PUT | `/api/teas/{teaId}/tasting-notes/{id}` | 更新品饮记录 |
| DELETE | `/api/teas/{teaId}/tasting-notes/{id}` | 删除品饮记录 |

## 开发说明

### 前端本地开发

```bash
cd frontend
npm install
npm run dev
```

访问：http://127.0.0.1:18041

### 后端本地开发

```bash
cd backend
mvn spring-boot:run
```

需要本地配置 MySQL 和 Redis，或修改 `application.yml` 中的连接地址。

## 许可证

MIT License
