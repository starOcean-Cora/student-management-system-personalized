# 学生管理系统

基于 Spring Boot 单体架构的学生管理系统通用骨架版。

## 技术栈

- Java 8
- Spring Boot 2.7.18
- Maven
- MySQL 5.7+
- Thymeleaf

## 功能模块

### 已实现

| 模块 | 管理员 | 学生 |
|------|--------|------|
| 登录 / 注册 / 退出 | ✅ 登录退出 | ✅ 登录注册退出 |
| 学生信息管理 | CRUD + 编辑全部字段 | 查看自己 + 修改头像 |
| 班级信息管理 | CRUD | 浏览 |
| 课程信息管理 | CRUD | 浏览 |
| 选课信息管理 | CRUD | 选课 / 取消 / 查看自己的选课 |
| 考勤信息管理 | CRUD | 查看自己的考勤 |
| 请假信息管理 | CRUD + 审批 | 申请 / 查看自己的请假 |
| 成绩信息管理 | CRUD（成绩列表） | 查看自己的成绩 |
| 成绩统计 | 筛选 + 统计卡片 + 柱状图 | ❌ |
| 修改密码 | ✅ | ✅ |

## 角色权限

- **学生**：可注册，注册后默认角色为学生。只能查看/操作自己的数据。
- **管理员**：由数据库初始化 SQL 插入，不开放管理员注册。拥有全部模块的增删改查权限，可以审批请假。
- 管理员账号同时代表教师角色。

## 数据库

### 环境要求

- MySQL 5.7 或更高版本
- Navicat 或其他 MySQL 客户端

### 数据库初始化

1. 在 Navicat 中选择你的本机 MySQL 连接，新建数据库：
   - 数据库名：`student_management`
   - 字符集：`utf8mb4`
   - 排序规则：`utf8mb4_general_ci`
2. 打开新建的数据库，新建查询
3. 将 `src/main/resources/schema.sql` 的全部内容粘贴到查询窗口，执行

> ⚠️ `schema.sql` 适合首次初始化。如果重复执行，可能会因用户名、学号、课程编号、选课记录、成绩记录等唯一约束报错。重复执行前请先删除数据库或清空相关数据。

### 数据库表

| 表名 | 说明 |
|------|------|
| `user_account` | 用户账号表 |
| `student_info` | 学生信息表 |
| `class_info` | 班级信息表 |
| `course_info` | 课程信息表 |
| `course_selection` | 选课信息表 |
| `attendance_info` | 考勤信息表 |
| `leave_info` | 请假信息表 |
| `grade_info` | 成绩信息表 |

## 运行步骤

### 1. 配置数据库连接

编辑 `src/main/resources/application.yml`，将 `username` 和 `password` 改为你的 MySQL 账号密码：

```yaml
spring:
  datasource:
    username: root     # 改为你的 MySQL 用户名
    password: 123456   # 改为你的 MySQL 密码
```

### 2. 启动项目

在项目根目录打开 PowerShell，执行：

```powershell
.\mvnw.cmd spring-boot:run
```

### 3. 访问系统

浏览器打开：http://localhost:8080

### 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | `admin` | `admin123` | 数据库初始化插入 |
| 学生 | `zhangsan` | `123456` | 测试数据，也可自行注册 |

> ⚠️ 以上默认密码仅用于本地实验演示，不要用于生产环境。如果运行期间修改过密码，以数据库当前数据为准。

## 项目结构

```
src/main/java/com/example/student/
├── StudentManagementApplication.java
├── config/
│   └── WebConfig.java                 # 拦截器配置
├── interceptor/
│   └── LoginInterceptor.java          # 登录拦截器
├── entity/
│   ├── UserAccount.java               # 用户账号
│   ├── StudentInfo.java               # 学生信息
│   ├── ClassInfo.java                 # 班级信息
│   ├── CourseInfo.java                # 课程信息
│   ├── CourseSelection.java           # 选课信息
│   ├── AttendanceInfo.java            # 考勤信息
│   ├── LeaveInfo.java                 # 请假信息
│   └── GradeInfo.java                 # 成绩信息
├── repository/
│   ├── UserAccountRepository.java
│   ├── StudentInfoRepository.java
│   ├── ClassInfoRepository.java
│   ├── CourseInfoRepository.java
│   ├── CourseSelectionRepository.java
│   ├── AttendanceInfoRepository.java
│   ├── LeaveInfoRepository.java
│   └── GradeInfoRepository.java
├── service/
│   ├── UserService.java
│   ├── StudentService.java
│   ├── ClassService.java
│   ├── CourseService.java
│   ├── CourseSelectionService.java
│   ├── AttendanceService.java
│   ├── LeaveService.java
│   └── GradeService.java
└── controller/
    ├── AuthController.java            # 登录/注册/退出
    ├── StudentController.java         # 学生管理
    ├── ClassController.java           # 班级管理
    ├── CourseController.java          # 课程管理
    ├── CourseSelectionController.java # 选课管理
    ├── AttendanceController.java      # 考勤管理
    ├── LeaveController.java           # 请假管理
    ├── GradeController.java           # 成绩管理+统计
    └── SystemController.java          # 修改密码

src/main/resources/
├── application.yml
├── schema.sql                         # 建库建表+测试数据
├── templates/
│   ├── login.html
│   ├── register.html
│   ├── index.html
│   ├── class/
│   ├── course/
│   ├── student/
│   ├── selection/
│   ├── attendance/
│   ├── leave/
│   ├── grade/
│   └── system/
└── static/
    ├── css/
    ├── js/
    └── images/
```

## 开发阶段

| 阶段 | 内容 | 状态 |
|------|------|------|
| 第一阶段 | 基础骨架 + 登录注册 + 角色区分 | ✅ |
| 第二阶段 | 基础资料管理（学生/班级/课程） | ✅ |
| 第三阶段 | 选课信息管理 | ✅ |
| 第四阶段 | 考勤/请假/成绩管理 | ✅ |
| 第五阶段 | 系统管理 + README + 发布 | 进行中 |

## 注意事项

1. **通用骨架版**：本项目不含任何学校名称、logo、背景图、介绍文字等学校定制内容。
2. **学校定制**：如需定制，建议单独开分支 `git checkout -b school-custom-version`，在 `src/main/resources/static/` 目录下替换背景图、logo、CSS 主题样式。
3. **安全性**：当前密码采用明文存储，仅适合课程实验演示，不要用于生产环境。
4. **数据库密码**：`application.yml` 中的数据库密码和 `schema.sql` 中的初始化账号密码提交到 Git 仓库前请确认不包含真实敏感信息。
5. **`.gitignore`**：确保 `target/`、`.idea/`、`*.iml` 等文件不被提交。

## GitHub / Gitee 发布检查清单

- [ ] `application.yml` 中无真实生产数据库密码
- [ ] `schema.sql` 中默认账号密码仅供实验演示
- [ ] 无学校名称、logo、背景图等定制内容
- [ ] `.gitignore` 已忽略 `target/`、`.idea/`、`*.iml`
- [ ] `README.md` 运行步骤完整可复现
- [ ] `git status` 确认无意外文件
- [ ] 提交信息清晰