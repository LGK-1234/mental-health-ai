# 心理健康 AI 助手 · 后端

基于 Spring Boot 3.5 + Spring AI 的心理健康咨询后端，提供 JWT 登录鉴权、AI 流式对话、咨询记录、情绪日记、知识库与数据概览接口。

## 技术栈

| 组件 | 版本 / 说明 |
| --- | --- |
| Spring Boot | 3.5.15 |
| Spring AI | 1.0.0（OpenAI 兼容协议，接硅基流动 DeepSeek-V3） |
| MyBatis-Plus | 3.5.7（含分页插件） |
| MySQL | 8.x，库名 `mental_health_assistant` |
| JWT | java-jwt 4.4.0 |
| JDK | 17 |

## 快速启动

### 1. 准备数据库

```sql
CREATE DATABASE IF NOT EXISTS mental_health_assistant DEFAULT CHARSET utf8mb4;
```

导入业务表（`user`、`knowledge_*`、`emotion_diary` 等），并执行：

```bash
mysql -uroot -p mental_health_assistant < sql/consultation.sql
```

> `consultation_session` / `consultation_message` 是 AI 对话必需的两张表。

### 2. 配置环境变量

```bat
set AI_API_KEY=你的硅基流动 API Key
set DB_USERNAME=root
set DB_PASSWORD=你的数据库密码
set JWT_SECRET=自定义签名密钥（生产必改）
```

不设置时使用 `application.yml` 中的默认值（仅适合本机演示）。

### 3. 启动

推荐用带 Key 提示的脚本：

```bat
run-dev.cmd
```

或手动：

```bat
mvnw.cmd spring-boot:run
```

启动后监听 **http://localhost:1236**。

## 接口一览

### 认证（公开）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/test` | 健康检查 |
| POST | `/api/user/login` | 登录，返回 token |
| POST | `/api/user/add` | 注册 |

### 用户（需登录）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/user/current` | 当前用户信息 |
| POST | `/api/user/logout` | 退出（JWT 无状态，仅语义） |

### AI 对话（需登录）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/psychological-chat/session/start` | 创建会话 |
| POST | `/api/psychological-chat/stream` | SSE 流式对话 |
| GET | `/api/psychological-chat/sessions` | 我的会话分页 |
| GET | `/api/psychological-chat/sessions/{id}/messages` | 会话消息明细 |
| DELETE | `/api/psychological-chat/sessions/{id}` | 删除会话 |
| GET | `/api/psychological-chat/session/{id}/emotion` | 会话情绪分析结果 |

### 情绪日记（需登录）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/emotion-diary` | 新增日记 |
| GET | `/api/emotion-diary/my` | 我的日记分页 |
| GET | `/api/emotion-diary/admin/page` | 管理端全部分页（带用户名） |
| DELETE | `/api/emotion-diary/admin/{id}` | 删除某条日记 |

### 知识库（需登录）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/knowledge/article/page` | 文章分页（title / categoryId / status 可选） |
| GET | `/api/knowledge/article/{id}` | 文章详情（阅读量 +1） |
| GET | `/api/knowledge/category/tree` | 分类树 |

### 数据概览（需登录）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/data-analytics/overview` | 看板数据：systemOverview / emotionTrend / consultationStats / userActivity |

### 知识库写操作（需登录）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/knowledge/article` | 新增文章（id 为空时自动生成 UUID） |
| PUT | `/api/knowledge/article/{id}` | 更新文章 |
| PUT | `/api/knowledge/article/{id}/status` | 改状态：0 草稿 / 1 发布 / 2 下线 |
| DELETE | `/api/knowledge/article/{id}` | 删除文章 |

### 文件上传（需登录）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/file/upload` | `multipart/form-data`，字段 `file`，可选 `businessType` / `businessId` / `businessField`，返回 `url` |

上传的文件保存在 `{工作目录}/uploads/{businessType}/`，通过 `/uploads/**` 直接访问（该路径无需登录）。

## 鉴权说明

请求头两种写法都支持：

```
Authorization: Bearer <token>
token: <token>
```

未携带或 token 无效统一返回：

```json
{ "code": "A0301", "msg": "访问未授权" }
```

## 目录结构

```
src/main/java/org/example/aispingboot/
├─ AiService/        AI 对话服务与提示词
├─ common/           统一返回 Result、异常处理
├─ config/           Security、JWT、ChatClient、MyBatis-Plus 分页
├─ controller/       各业务接口
├─ DTO/              请求/响应对象
├─ entity/           实体类
├─ mapper/           MyBatis-Plus Mapper
├─ service/          业务逻辑
└─ util/             JWT 工具、过滤器、响应工具
```

## 常见问题

**1. 报 `程序包 org.springframework.ai.chat.client 不存在`**

pom 里 Spring AI 版本写成了 `1.0.0-SNAPSHOT`，而 SNAPSHOT 只发布在 `repo.spring.io/snapshot`，且项目没有配置该仓库，依赖无法解析。改为正式版 `1.0.0` 后重新加载 Maven 即可。

**2. 登录成功但访问接口一律 401**

检查 token 是否放在 `Authorization: Bearer xxx` 或 `token: xxx` 请求头中。后端会按 `jwt.header` / `jwt.token-prefix` 读取并自动去掉前缀。

**3. AI 回复报 401 / 402**

- 401：`AI_API_KEY` 没配置或无效
- 402 `Sorry, your account balance is insufficient`：key 有效但硅基流动账户余额不足，充值后即可

配置方式：

```bat
set AI_API_KEY=sk-xxx
mvnw.cmd spring-boot:run
```

**4. 分页 total 一直是 0**

缺少 MyBatis-Plus 分页插件，已通过 `config/MybatisPlusConfig` 注册 `PaginationInnerInterceptor`。

## 安全提示

- `application.yml` 中的密钥、密码均为占位默认值，实际部署请用环境变量覆盖
- 生产环境务必修改 `JWT_SECRET`
- 密码使用 BCrypt 存储
