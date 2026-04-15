# HotTrend Spring

热点新闻聚合与分析工具 - Spring Boot 实现

## 技术栈

### 核心框架
- **Spring Boot 3.5.9** - 应用框架
- **Java 21** - 运行环境

### 数据层
- **MyBatis-Plus 3.5.16** - ORM 框架（替代 JPA）
- **MySQL 8.0** - 关系型数据库
- **Redis** - 缓存数据库

### 缓存与性能
- **Spring Data Redis** - Redis 客户端
- **Spring Cache** - 缓存抽象层

### 定时任务
- **Spring Scheduler** - 定时任务调度

### 其他
- **Lombok** - 代码简化
- **Jackson** - JSON 处理
- **Rome** - RSS 解析
- **OpenAI SDK** - AI 分析（可选）

## 项目架构

```
hottrend-spring
├── src/main/java/com/hottrend/
│   ├── config/          # 配置类
│   │   ├── MybatisPlusConfig.java    # MyBatis-Plus 配置
│   │   └── RedisConfig.java          # Redis 配置
│   ├── controller/      # REST API 控制器
│   │   ├── NewsController.java       # 新闻管理
│   │   ├── KeywordController.java    # 关键词管理
│   │   ├── RssController.java        # RSS 订阅管理
│   │   └── WebController.java        # Web 页面
│   ├── crawler/         # 数据抓取
│   │   ├── CrawlerService.java       # 热榜抓取服务
│   │   ├── PlatformCrawler.java      # 平台抓取器接口
│   │   └── rss/
│   │       └── RssCrawlerService.java # RSS 抓取服务
│   ├── mapper/          # 数据访问层（MyBatis-Plus）
│   │   ├── NewsMapper.java
│   │   ├── KeywordMapper.java
│   │   ├── PlatformMapper.java
│   │   ├── RssSourceMapper.java
│   │   ├── RssItemMapper.java
│   │   └── NotificationMapper.java
│   ├── model/           # 数据实体
│   │   ├── NewsItem.java        # 新闻
│   │   ├── Keyword.java         # 关键词
│   │   ├── Platform.java        # 平台
│   │   ├── RssSource.java       # RSS 源
│   │   ├── RssItem.java         # RSS 文章
│   │   └── NotificationRecord.java # 通知记录
│   ├── notification/    # 通知模块
│   │   ├── NotificationService.java    # 通知服务
│   │   ├── TelegramNotificationSender.java
│   │   ├── WebhookNotificationSender.java
│   │   └── EmailNotificationSender.java
│   ├── scheduler/       # 定时任务
│   │   └── SchedulerService.java
│   ├── ai/              # AI 分析（可选）
│   │   ├── AIService.java
│   │   └── OpenAIService.java
│   └── report/          # 报告生成
│       └── HtmlReportService.java
└── src/main/resources/
    └── application.yml  # 应用配置
```

## 核心模块

### 1. 数据抓取模块 (crawler)
- 支持多个平台的热榜数据抓取（微博、百度、抖音、知乎等）
- 支持 RSS 订阅源抓取
- 自动去重和更新

### 2. 数据存储层 (mapper)
- 基于 MyBatis-Plus 的数据访问
- 支持分页、条件查询
- 逻辑删除支持

### 3. 定时任务 (scheduler)
- 每小时自动抓取数据
- 早晚两次推送报告
- 每日 AI 分析任务
- 自动清理旧数据

### 4. 通知模块 (notification)
- 支持多种通知渠道：
  - 企业微信
  - Telegram
  - 钉钉
  - 飞书
  - 邮件

### 5. 缓存层 (Redis)
- Redis 缓存支持
- Spring Cache 抽象
- JSON 序列化

## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: *
    username: *
    password: *
```

### Redis 配置
```yaml
spring:
  data:
    redis:
      host: *
      port: 6379
```

### 定时任务配置
```yaml
scheduler:
  enabled: true
  preset: "morning_evening"  # 早晚报
```

## 快速开始

### 1. 环境要求
- JDK 21+
- Maven 3.9.6+
- MySQL 8.0.23+
- Redis（可选）

### 2. 创建数据库
```sql
CREATE DATABASE hottrend DEFAULT CHARACTER SET utf8mb4;
```

### 3. 修改配置
编辑 `src/main/resources/application.yml`，配置 MySQL 和 Redis 连接信息。

### 4. 编译运行
```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run
```

### 5. 访问
- Web 界面: http://localhost:8080
- API: http://localhost:8080/api/news

## API 接口

### 新闻接口
- `GET /api/news` - 获取新闻列表
- `GET /api/news/{id}` - 获取新闻详情
- `GET /api/news/stats` - 获取统计信息
- `POST /api/news/fetch` - 手动抓取
- `POST /api/news/push` - 手动推送

### 关键词接口
- `GET /api/keywords` - 获取所有关键词
- `POST /api/keywords` - 添加关键词
- `DELETE /api/keywords/{id}` - 删除关键词

### RSS 接口
- `GET /api/rss/sources` - 获取 RSS 源列表
- `POST /api/rss/sources` - 添加 RSS 源
- `POST /api/rss/fetch` - 手动抓取 RSS

## 扩展开发

### 添加新的抓取器
1. 实现 `PlatformCrawler` 接口
2. 在 `CrawlerService` 中注册
3. 配置启用

### 添加新的通知渠道
1. 实现 `NotificationSender` 接口
2. 注册为 Spring Bean

## License

MIT