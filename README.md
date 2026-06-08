# 学生管理系统

基于 Spring Boot 单体架构的学生管理系统通用骨架版。

## 技术栈

- Java 8
- Spring Boot 2.7.18
- Maven
- MySQL 5.7+
- Thymeleaf

## 第一阶段：基础骨架 + 登录注册 + 角色区分

当前已实现：
- 学生注册（用户名/密码/学号/姓名）
- 用户登录
- 退出登录
- 学生/管理员角色区分
- 登录后根据角色显示不同菜单

## 快速开始

### 1. 数据库准备（Navicat）

在 Navicat 中选择你的本机 MySQL 连接，新建数据库：

- 数据库名：`student_management`
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_general_ci`

打开新建的数据库，新建查询，将 `src/main/resources/schema.sql` 的全部内容粘贴到查询窗口，执行。

> ⚠️ `schema.sql` 适合首次初始化。如果重复执行，可能会因唯一约束报错。重复执行前请先删除数据库或清空相关数据。

### 2. 修改数据库密码

编辑 `src/main/resources/application.yml`，将 `username` 和 `password` 改为你的 MySQL 账号密码。

### 3. 启动项目

```bash
mvn spring-boot:run
```

或使用项目自带的 Maven Wrapper：

```bash
.\mvnw.cmd spring-boot:run
```

### 4. 访问系统

浏览器打开：http://localhost:8080

- **默认管理员账号**：`admin` / `admin123`
- **测试学生账号**（schema.sql 初始化）：`zhangsan` / `123456`
- 也可通过注册页面自行注册学生账号

> ⚠️ `admin/admin123` 和 `123456` 仅用于本地实验演示，不要用于生产环境。

## 项目结构

```
src/main/java/com/example/student/
├── StudentManagementApplication.java    # 启动类
├── config/WebConfig.java                # 拦截器配置
├── interceptor/LoginInterceptor.java    # 登录拦截器
├── entity/
│   ├── UserAccount.java                 # 用户账号实体
│   └── StudentInfo.java                 # 学生信息实体
├── repository/
│   ├── UserAccountRepository.java       # 用户 JPA 仓库
│   └── StudentInfoRepository.java       # 学生信息 JPA 仓库
├── service/UserService.java             # 注册 + 登录验证
└── controller/AuthController.java       # 登录/注册/退出

src/main/resources/
├── application.yml                      # 配置文件
├── schema.sql                           # 建库建表 + 测试数据 SQL
└── templates/
    ├── login.html                       # 登录页
    ├── register.html                    # 注册页
    └── index.html                       # 首页（角色感知）
```

## 后续开发

本项目为通用骨架版。后续可按 AGENTS.md 分阶段计划逐步实现学生管理、班级管理、课程管理、选课管理、考勤管理、请假管理、成绩管理等模块。

学校定制版建议单独开 Git 分支：

```bash
git checkout -b school-custom-version
```
