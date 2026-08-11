# 智能作业批改系统后端

基于Spring Boot + MySQL + Redis + DeepSeek AI的智能作业批改系统后端服务。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 6.0+ | 缓存数据库 |
| DeepSeek | API | AI智能批改 |
| JWT | 4.4.0 | 身份认证 |
| Maven | 3.8+ | 构建工具 |

## 项目结构

```
backend/
├── src/main/java/com/balabala/homework/
│   ├── HomeworkCorrectionApplication.java  # 启动类
│   ├── config/                             # 配置类
│   │   └── SecurityConfig.java
│   ├── controller/                         # 控制器层
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── AIController.java
│   │   ├── ClassController.java
│   │   ├── QuestionController.java
│   │   └── HomeworkController.java
│   ├── service/                            # 服务层
│   │   ├── DeepSeekService.java           # DeepSeek AI服务
│   │   ├── UserService.java
│   │   ├── ClassService.java
│   │   ├── QuestionService.java
│   │   └── HomeworkService.java
│   ├── repository/                         # 数据访问层
│   │   ├── UserRepository.java
│   │   ├── ClassRepository.java
│   │   ├── QuestionRepository.java
│   │   ├── HomeworkRepository.java
│   │   └── SubmissionRepository.java
│   ├── entity/                             # 实体类
│   │   ├── User.java
│   │   ├── ClassInfo.java
│   │   ├── Question.java
│   │   ├── Homework.java
│   │   ├── Submission.java
│   │   └── Answer.java
│   ├── dto/                                # 数据传输对象
│   │   ├── Result.java
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── AICorrectRequest.java
│   └── util/                               # 工具类
│       └── JwtUtil.java
├── src/main/resources/
│   └── application.yml                     # 配置文件
└── pom.xml                                 # Maven配置
```

## 核心功能

### 1. 用户认证模块
- JWT Token认证
- 用户注册/登录
- 角色权限控制（管理员/教师/学生）

### 2. 班级管理模块
- 班级CRUD
- 学生管理
- 邀请码加入班级

### 3. 作业管理模块
- 发布作业/考试
- 班级关联
- 截止时间控制

### 4. 题目管理模块
- 题库管理
- 多种题型支持（选择/填空/判断/简答/阅读）
- 知识点标签

### 5. AI智能批改模块
- **DeepSeek API集成**
- **RAG检索增强** - 基于知识库的精准批改
- **Agent智能体** - 多轮推理深度分析
- **Prompt工程** - 结构化输出评分结果

## AI功能详解

### 智能批改流程

```
学生提交答案
    ↓
AI批改服务
├── 基础批改 (DeepSeek API)
├── RAG批改 (知识库检索)
└── Agent批改 (多轮推理)
    ↓
返回评分结果
├── 得分
├── 置信度
├── 维度分析（准确性/完整性/逻辑性）
├── 详细点评
└── 改进建议
```

### API接口

#### AI批改接口

```java
// 基础批改
POST /api/ai/correct
{
    "question": "题目内容",
    "studentAnswer": "学生答案",
    "correctAnswer": "参考答案",
    "questionType": "short_answer",
    "fullScore": 10
}

// RAG批改
POST /api/ai/correct/rag
{
    "question": "题目内容",
    "studentAnswer": "学生答案",
    "knowledgePoint": "修辞手法"
}

// Agent批改
POST /api/ai/correct/agent
{
    "question": "题目内容",
    "studentAnswer": "学生答案",
    "correctAnswer": "参考答案",
    "questionType": "short_answer",
    "fullScore": 10
}
```

## 快速开始

### 1. 环境准备

```bash
# 安装MySQL并创建数据库
create database homework_db character set utf8mb4 collate utf8mb4_unicode_ci;

# 安装Redis
redis-server
```

### 2. 配置DeepSeek API Key

```bash
# 设置环境变量
export DEEPSEEK_API_KEY=your-api-key-here
```

### 3. 运行项目

```bash
# 编译运行
./mvnw spring-boot:run

# 或打包后运行
./mvnw clean package
java -jar target/homework-correction-system-1.0.0.jar
```

### 4. 访问服务

- API地址: http://localhost:8080/api
- 健康检查: http://localhost:8080/actuator/health

## 数据库表结构

### 核心表

| 表名 | 说明 |
|------|------|
| users | 用户表 |
| classes | 班级表 |
| questions | 题目表 |
| homeworks | 作业表 |
| submissions | 提交记录表 |
| answers | 答题记录表 |
| error_books | 错题本表 |

## 配置说明

### application.yml

```yaml
# 数据库配置
spring.datasource.url: jdbc:mysql://localhost:3306/homework_db
spring.datasource.username: root
spring.datasource.password: 123456

# Redis配置
spring.data.redis.host: localhost
spring.data.redis.port: 6379

# DeepSeek配置
deepseek.api.key: ${DEEPSEEK_API_KEY}
deepseek.api.url: https://api.deepseek.com/v1/chat/completions
deepseek.api.model: deepseek-chat
```

## 开发计划

- [x] 项目基础架构
- [x] 数据库设计
- [x] 用户认证模块
- [x] 班级管理模块
- [x] 作业管理模块
- [x] 题目管理模块
- [x] DeepSeek AI集成
- [x] RAG检索增强
- [x] Agent智能体
- [ ] 向量数据库集成
- [ ] 文件上传/解析
- [ ] 统计分析模块

## 注意事项

1. **DeepSeek API Key**: 需要在环境变量中配置
2. **数据库编码**: 使用utf8mb4以支持emoji
3. **Redis**: 用于缓存和会话管理
4. **CORS**: 生产环境需要配置跨域

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交代码
4. 创建Pull Request

## License

MIT License
