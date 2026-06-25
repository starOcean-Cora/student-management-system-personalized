# 基于单体架构的学生管理系统设计与实现

## 1 引言

随着高校信息化建设的持续推进，学生管理工作的数字化、系统化已成为高校日常运转的重要支撑。传统的人工纸质管理或电子表格管理在面对日益增长的学生基础信息、课程数据、成绩记录、考勤与请假等多维度信息时，逐渐暴露出效率低下、数据一致性难保证、多角色协作困难等问题。开发一套功能完备、易于操作、角色明确的在线学生管理系统，对于提升高校日常管理效率、降低人工出错率具有显著的现实意义。

从应用角度而言，本系统面向两类核心用户——学生和管理员（兼教师）。学生可通过系统完成账号注册、个人信息查看与头像维护，并能够在统一的平台上浏览班级与课程信息、进行选课、查看考勤与成绩、提交请假申请等操作。管理员则承载着全部基础资料（学生、班级、课程）的维护职责，同时负责选课管理、考勤管理、请假审批、成绩录入与统计等一系列教学管理核心业务。

从技术角度而言，本项目采用 Java 语言和 Spring Boot 框架构建单体架构的 Web 应用，使用 MySQL 关系数据库实现数据的持久化存储，通过 Thymeleaf 模板引擎完成服务端渲染，整体遵循 MVC（Model-View-Controller）分层设计模式。系统原则上不引入微服务、Nacos、Gateway、Redis 等复杂中间件，以事务脚本模式为核心架构风格，按照"业务需求→概要设计→数据库设计→详细设计与实现"的开发流程推进。该技术组合成熟稳定、学习曲线平缓，适合课程设计阶段的快速开发和教学实践。

## 2 需求分析与概要设计

### 2.1 需求分析

本系统的参与者分为两类：学生（Student）和管理员（Admin，同时兼任教师角色）。学生为系统的核心使用群体，可通过公开注册获取账号，注册成功后默认获得学生角色。管理员账号不开放注册，由数据库初始化脚本预先创建。

学生用例如下：学生可执行账号注册与登录操作，能够查看自己的基本信息并修改头像，可浏览班级列表与课程列表，可在可选课程范围内进行选课操作并查看自己的选课结果，能够查看自己的考勤记录，能够提交请假申请并追踪请假审批状态，能够查看自己的全部成绩记录（按考试类型分类展示），可以修改个人登录密码。

管理员的用例覆盖范围更为广泛：在学生信息管理方面，管理员可对学生信息进行增删改查，可修改学号、姓名、性别、班级归属、联系方式及学生状态等全部字段。在班级管理方面，管理员可完成班级的新增、编辑和删除操作。在课程管理方面，管理员可对课程编号、课程名称、授课教师、学分、课时等信息进行增删改查。在选课管理方面，管理员可查看全部学生的选课记录，可代为新增或删除选课。在考勤管理方面，管理员可对学生的考勤状态进行新增、编辑和删除操作，考勤状态包含出勤、迟到、早退、缺勤和请假五种。在请假管理方面，管理员可查看全部请假申请，并对待审批状态的请假记录执行通过或驳回的审批操作。在成绩管理方面，管理员可录入学生成绩（需指定考试类型：期中考试、期末考试或平时成绩），可编辑已有成绩或删除成绩记录，系统自动根据分值计算等级（优秀/良好/中等/及格/不及格）和状态（已录入/需关注）。此外，管理员可通过成绩统计页面按课程、班级和考试类型进行筛选，查看平均分、最高分、最低分、及格率以及各分数区间的分布柱状图。

学生用例图如图2-1所示，管理员用例图如图2-2所示。

*（此处插入图2-1 学生用例图）*

*（此处插入图2-2 管理员用例图）*

### 2.2 软件的概要设计

**业务架构**：系统按照功能域划分为九个核心模块：注册登录模块、学生信息管理模块、班级信息管理模块、课程信息管理模块、选课信息管理模块、考勤信息管理模块、请假管理模块、成绩管理模块（含成绩统计）以及系统管理（修改密码）模块。各模块通过统一的会话（HttpSession）实现登录状态保持与角色判定，根据角色动态控制页面菜单和操作权限的可见性，同时在 Controller 层对每一个写操作路由进行服务端角色校验。系统业务架构图如图2-3所示。

*（此处插入图2-3 系统业务架构图）*

**应用架构**：系统采用经典的 MVC 三层架构。Controller 层负责接收 HTTP 请求、校验 session 权限、调用 Service 层方法，并将处理结果通过 Model 传递给 Thymeleaf 模板进行页面渲染。Service 层封装核心业务逻辑，包括用户认证与注册、学生成绩等级自动计算、选课重复性校验、成绩唯一性校验、请假审批流程等，并承担跨 Repository 的数据协调。Repository 层通过 Spring Data JPA 接口完成实体对象的持久化操作，与 MySQL 数据库直接交互。拦截器（LoginInterceptor）以 Spring WebMvcConfigurer 注册方式对除登录、注册及静态资源外的所有请求进行登录状态检查。应用架构如图2-4所示。

*（此处插入图2-4 系统应用架构图）*

**技术架构**：系统技术选型如下表2-1所示。

表2-1 系统技术选型

| 层次 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 开发语言 | Java | 8 | 项目基础语言 |
| 核心框架 | Spring Boot | 2.7.18 | 应用容器与自动配置 |
| 构建工具 | Maven | 3.x | 依赖管理与项目构建 |
| 数据持久化 | Spring Data JPA / Hibernate | — | ORM 框架，实体映射与 CRUD |
| 数据库 | MySQL | 5.7+ | 关系型数据库，InnoDB 引擎 |
| 模板引擎 | Thymeleaf | — | 服务端 HTML 渲染 |
| 前端样式 | 原生 CSS | — | 布局与玻璃拟态（Glassmorphism）效果 |
| JDBC 驱动 | MySQL Connector/J | — | 数据库连接驱动 |

系统整体遵循单体架构模式，所有业务模块运行在同一个 Spring Boot 应用进程中，共享同一数据源。表示层、业务逻辑层和数据访问层在同一个 JVM 内通过方法调用直接通信，不涉及远程过程调用或消息中间件。该架构部署简单、开发调试便捷、事务管理直观，适合中等规模的课程设计项目。

## 3 数据库设计

### 3.1 概念结构设计

经过需求分析，抽象出系统的核心实体及关系如下：

- **用户账号（UserAccount）**与**学生信息（StudentInfo）**之间为一对一关联关系：每个学生用户在注册时同步创建一条学生信息记录和一条用户账号记录，user_account.related_student_id 外键指向 student_info.id；管理员用户的 related_student_id 为 NULL。
- **班级（ClassInfo）**与**学生信息（StudentInfo）**之间为一对多关系：一个班级可包含多名学生，每名学生归属于一个班级（允许为空）。
- **学生（StudentInfo）**与**课程（CourseInfo）**之间为多对多关系，通过**选课记录（CourseSelection）**作为中间关联表实现：每条选课记录将一名学生与一门课程绑定，且同一学生不得重复选择同一课程。
- **考勤记录（AttendanceInfo）**关联学生与课程：每条考勤记录记录某学生在某门课程某日的出勤状态。
- **请假记录（LeaveInfo）**仅关联学生：每条请假记录属于一名学生，记录请假类型、原因、起止时间及审批状态。管理员用户对请假记录进行审批。
- **成绩记录（GradeInfo）**关联学生与课程：每条成绩记录对应于一名学生在某门课程的某次考试类型（期中/期末/平时）下的成绩分值。同一学生、同一课程、同一考试类型不可重复录入。

系统全局 E-R 图如图3-1所示，展示了上述实体及其之间的关联关系、主键属性以及核心普通属性。

*（此处插入图3-1 数据库E-R图）*

### 3.2 逻辑结构设计

#### 3.2.1 关系模式

将 E-R 图转换为关系模式，得到以下关系（主键以下划线标识，外键以斜体标识）：

1. **class_info**（<u>id</u>，class_name，grade，major，teacher_name，remark，create_time，update_time）
2. **course_info**（<u>id</u>，course_code，course_name，teacher_name，credit，class_hours，remark，create_time，update_time），其中 course_code 为唯一键
3. **student_info**（<u>id</u>，student_no，name，gender，*class_id*，class_name，phone，email，avatar，status，create_time，update_time），其中 student_no 为唯一键，class_id 外键参照 class_info(id)
4. **user_account**（<u>id</u>，username，password，role，*related_student_id*，avatar，status，create_time，update_time），其中 username 为唯一键，related_student_id 外键参照 student_info(id)
5. **course_selection**（<u>id</u>，*student_id*，student_no，student_name，*class_id*，class_name，*course_id*，course_name，select_time，status，create_time，update_time），其中 (student_id，course_id) 为唯一键，student_id 外键参照 student_info(id)，course_id 外键参照 course_info(id)，class_id 外键参照 class_info(id)
6. **attendance_info**（<u>id</u>，*student_id*，student_no，student_name，*class_id*，class_name，*course_id*，course_name，attendance_date，status，remark，create_time，update_time），其中 student_id 外键参照 student_info(id)，course_id 外键参照 course_info(id)，class_id 外键参照 class_info(id)
7. **leave_info**（<u>id</u>，*student_id*，student_no，student_name，*class_id*，class_name，leave_type，reason，start_time，end_time，status，*approver_id*，approve_time，approve_remark，remark，create_time，update_time），其中 student_id 外键参照 student_info(id)，class_id 外键参照 class_info(id)，approver_id 外键参照 user_account(id)
8. **grade_info**（<u>id</u>，*student_id*，student_no，student_name，*class_id*，class_name，*course_id*，course_name，exam_type，score，grade_level，status，remark，create_time，update_time），其中 (student_id，course_id，exam_type) 为唯一键，student_id 外键参照 student_info(id)，course_id 外键参照 course_info(id)，class_id 外键参照 class_info(id)

在选课、考勤、请假、成绩等表中采用了部分字段冗余设计（如 student_no、student_name、class_name、course_name 等），目的是在列表查询与展示场景下避免频繁的多表 JOIN 操作，提升查询效率，同时保证当学生或课程的源信息发生变更时，通过应用层逻辑同步更新相关冗余字段。

#### 3.2.2 表结构设计

表3-1 class_info 班级信息表

| 字段名 | 数据类型 | 允许空 | 键/约束 | 说明 |
|--------|---------|--------|---------|------|
| id | BIGINT | 否 | PK，AUTO_INCREMENT | 主键 |
| class_name | VARCHAR(100) | 否 | — | 班级名称 |
| grade | VARCHAR(50) | 是 | — | 年级 |
| major | VARCHAR(100) | 是 | — | 专业 |
| teacher_name | VARCHAR(50) | 是 | — | 班主任/教师 |
| remark | VARCHAR(500) | 是 | — | 备注 |
| create_time | DATETIME | 否 | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | 是 | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

表3-2 course_info 课程信息表

| 字段名 | 数据类型 | 允许空 | 键/约束 | 说明 |
|--------|---------|--------|---------|------|
| id | BIGINT | 否 | PK，AUTO_INCREMENT | 主键 |
| course_code | VARCHAR(30) | 否 | UNIQUE | 课程编号 |
| course_name | VARCHAR(100) | 否 | — | 课程名称 |
| teacher_name | VARCHAR(50) | 是 | — | 授课教师 |
| credit | DECIMAL(3，1) | 是 | — | 学分 |
| class_hours | INT | 是 | — | 课时 |
| remark | VARCHAR(500) | 是 | — | 备注 |
| create_time | DATETIME | 否 | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | 是 | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

表3-3 student_info 学生信息表

| 字段名 | 数据类型 | 允许空 | 键/约束 | 说明 |
|--------|---------|--------|---------|------|
| id | BIGINT | 否 | PK，AUTO_INCREMENT | 主键 |
| student_no | VARCHAR(30) | 否 | UNIQUE | 学号 |
| name | VARCHAR(50) | 否 | — | 姓名 |
| gender | TINYINT | 是 | — | 性别（0=女，1=男） |
| class_id | BIGINT | 是 | FK→class_info.id | 班级ID |
| class_name | VARCHAR(100) | 是 | — | 班级名称（冗余） |
| phone | VARCHAR(20) | 是 | — | 手机号 |
| email | VARCHAR(100) | 是 | — | 邮箱 |
| avatar | VARCHAR(255) | 是 | — | 头像路径 |
| status | TINYINT | 否 | DEFAULT 1 | 状态（1=在读，0=离校） |
| create_time | DATETIME | 否 | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | 是 | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

表3-4 user_account 用户账号表

| 字段名 | 数据类型 | 允许空 | 键/约束 | 说明 |
|--------|---------|--------|---------|------|
| id | BIGINT | 否 | PK，AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | 否 | UNIQUE | 用户名 |
| password | VARCHAR(100) | 否 | — | 密码（明文存储，仅用于实验演示） |
| role | VARCHAR(20) | 否 | — | 角色（STUDENT/ADMIN） |
| related_student_id | BIGINT | 是 | FK→student_info.id | 关联学生ID（管理员为NULL） |
| avatar | VARCHAR(255) | 是 | — | 头像路径 |
| status | TINYINT | 否 | DEFAULT 1 | 状态（1=启用，0=禁用） |
| create_time | DATETIME | 否 | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | 是 | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

表3-5 course_selection 选课信息表

| 字段名 | 数据类型 | 允许空 | 键/约束 | 说明 |
|--------|---------|--------|---------|------|
| id | BIGINT | 否 | PK，AUTO_INCREMENT | 主键 |
| student_id | BIGINT | 否 | FK→student_info.id | 学生ID |
| student_no | VARCHAR(30) | 否 | — | 学号（冗余） |
| student_name | VARCHAR(50) | 否 | — | 学生姓名（冗余） |
| class_id | BIGINT | 是 | FK→class_info.id | 班级ID |
| class_name | VARCHAR(100) | 是 | — | 班级名称（冗余） |
| course_id | BIGINT | 否 | FK→course_info.id | 课程ID |
| course_name | VARCHAR(100) | 否 | — | 课程名称（冗余） |
| select_time | DATETIME | 是 | — | 选课时间 |
| status | VARCHAR(20) | 是 | — | 选课状态（已选/退选） |
| create_time | DATETIME | 否 | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | 是 | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**约束**：UNIQUE(student_id，course_id)——同一学生不得重复选择同一课程。

表3-6 attendance_info 考勤信息表

| 字段名 | 数据类型 | 允许空 | 键/约束 | 说明 |
|--------|---------|--------|---------|------|
| id | BIGINT | 否 | PK，AUTO_INCREMENT | 主键 |
| student_id | BIGINT | 否 | FK→student_info.id | 学生ID |
| student_no | VARCHAR(30) | 否 | — | 学号（冗余） |
| student_name | VARCHAR(50) | 否 | — | 学生姓名（冗余） |
| class_id | BIGINT | 是 | FK→class_info.id | 班级ID |
| class_name | VARCHAR(100) | 是 | — | 班级名称（冗余） |
| course_id | BIGINT | 否 | FK→course_info.id | 课程ID |
| course_name | VARCHAR(100) | 否 | — | 课程名称（冗余） |
| attendance_date | DATE | 否 | — | 考勤日期 |
| status | VARCHAR(20) | 否 | — | 状态（出勤/迟到/早退/缺勤/请假） |
| remark | VARCHAR(500) | 是 | — | 备注 |
| create_time | DATETIME | 否 | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | 是 | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

表3-7 leave_info 请假信息表

| 字段名 | 数据类型 | 允许空 | 键/约束 | 说明 |
|--------|---------|--------|---------|------|
| id | BIGINT | 否 | PK，AUTO_INCREMENT | 主键 |
| student_id | BIGINT | 否 | FK→student_info.id | 学生ID |
| student_no | VARCHAR(30) | 否 | — | 学号（冗余） |
| student_name | VARCHAR(50) | 否 | — | 学生姓名（冗余） |
| class_id | BIGINT | 是 | FK→class_info.id | 班级ID |
| class_name | VARCHAR(100) | 是 | — | 班级名称（冗余） |
| leave_type | VARCHAR(20) | 否 | — | 请假类型（事假/病假/其他） |
| reason | VARCHAR(500) | 否 | — | 请假原因 |
| start_time | DATETIME | 否 | — | 开始时间 |
| end_time | DATETIME | 否 | — | 结束时间 |
| status | VARCHAR(20) | 否 | DEFAULT '待审批' | 审批状态（待审批/已通过/已驳回） |
| approver_id | BIGINT | 是 | FK→user_account.id | 审批人ID |
| approve_time | DATETIME | 是 | — | 审批时间 |
| approve_remark | VARCHAR(500) | 是 | — | 审批备注 |
| remark | VARCHAR(500) | 是 | — | 学生备注 |
| create_time | DATETIME | 否 | DEFAULT CURRENT_TIMESTAMP | 申请时间 |
| update_time | DATETIME | 是 | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

表3-8 grade_info 成绩信息表

| 字段名 | 数据类型 | 允许空 | 键/约束 | 说明 |
|--------|---------|--------|---------|------|
| id | BIGINT | 否 | PK，AUTO_INCREMENT | 主键 |
| student_id | BIGINT | 否 | FK→student_info.id | 学生ID |
| student_no | VARCHAR(30) | 否 | — | 学号（冗余） |
| student_name | VARCHAR(50) | 否 | — | 学生姓名（冗余） |
| class_id | BIGINT | 是 | FK→class_info.id | 班级ID |
| class_name | VARCHAR(100) | 是 | — | 班级名称（冗余） |
| course_id | BIGINT | 否 | FK→course_info.id | 课程ID |
| course_name | VARCHAR(100) | 否 | — | 课程名称（冗余） |
| exam_type | VARCHAR(20) | 否 | — | 考试类型（期中考试/期末考试/平时成绩） |
| score | DECIMAL(5，1) | 否 | CHECK 0~100 | 成绩分值 |
| grade_level | VARCHAR(20) | 否 | — | 等级（优秀/良好/中等/及格/不及格） |
| status | VARCHAR(20) | 否 | DEFAULT '已录入' | 状态（已录入/需关注） |
| remark | VARCHAR(500) | 是 | — | 备注 |
| create_time | DATETIME | 否 | DEFAULT CURRENT_TIMESTAMP | 录入时间 |
| update_time | DATETIME | 是 | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**约束**：UNIQUE(student_id，course_id，exam_type)——同一学生、同一课程、同一考试类型不得重复录入。CHECK(score >= 0 AND score <= 100)——数据库层和应用层双重校验分值范围。

成绩等级由应用层 `@PrePersist`/`@PreUpdate` 自动计算：score >= 90 → "优秀"，80-89 → "良好"，70-79 → "中等"，60-69 → "及格"，0-59 → "不及格"。成绩状态同步计算：score >= 60 → "已录入"，score < 60 → "需关注"。

## 4 软件详细设计

### 4.1 核心业务的详细设计

本节对系统中若干关键业务流程进行详细设计描述，以时序图和流程图相结合的方式展现各模块的交互过程与业务判断逻辑。

#### 4.1.1 登录认证

用户访问系统任意受保护页面时，LoginInterceptor 拦截请求并检查 HttpSession 中是否存在 userId 属性。若不存在，重定向至 `/login` 登录页面；若存在，放行至目标 Controller。已放行的公开路径包括 `/login`、`/register`、`/logout` 以及 `/images/**`、`/css/**`、`/js/**` 等静态资源路径。

用户在登录页面提交用户名与密码后，AuthController 将表单数据交由 UserService 进行验证。UserService 通过 UserAccountRepository 按用户名查询数据库中的用户记录，校验密码是否匹配（明文比对）以及账号状态（status = 1）是否启用。校验通过后，将 userId、username、role、relatedStudentId 四个属性写入 HttpSession。随后前端根据 session 中的 role 属性动态渲染学生菜单或管理员菜单。登录认证时序图如图4-1所示。

*（此处插入图4-1 登录认证时序图）*

#### 4.1.2 学生选课

学生登录后访问 `/selection/my` 页面，CourseSelectionController 从 session 中获取当前学生的 relatedStudentId，调用 CourseSelectionService 查询该生已有的选课记录，同时从 CourseInfoRepository 加载全部课程列表。Controller 将已选课程的 courseId 放入 Set 集合，过滤出未被该生选择的课程作为"可选课程列表"，与"已选课程记录"一同返回给前端模板。前端页面分两个区域展示：上方为可选课程表格，每行附带"选课"按钮；下方为已选课程记录，每行附带"取消"按钮。

学生点击某课程的"选课"按钮时，Controller 首先校验 studentId 与 courseId 是否已存在选课记录（isAlreadySelected 方法），若已存在则返回错误提示"不能重复选课"；若不存在则创建新的 CourseSelection 实体，自动从 StudentInfo 冗余填充 student_no、student_name、class_id、class_name，从 CourseInfo 冗余填充 course_name，设置 select_time 为当前时间和 status 为"已选"，最后保存到数据库并返回"选课成功"提示。学生选课时序图如图4-2所示。

*（此处插入图4-2 学生选课时序图）*

#### 4.1.3 请假申请与审批

学生登录后在 `/leave/apply` 页面填写请假类型（事假/病假/其他）、请假原因、开始时间、结束时间和可选备注，表单以 POST 方式提交至 LeaveController。Controller 从 session 获取当前学生的 relatedStudentId，创建 LeaveInfo 实体并赋值学生 ID 及表单字段，初始状态设置为"待审批"，调用 LeaveService.fillRedundantFields 自动从 StudentInfo 冗余填充学号、姓名、班级等信息，保存至数据库。

管理员在 `/leave` 列表页面可查看全部请假记录。对于状态为"待审批"的记录，管理员可点击"审批"按钮进入 `/leave/approve/{id}` 页面。该页面展示请假详情（学生、班级、类型、原因、时间段），并提供审批结果下拉选择（已通过/已驳回）和审批备注输入框。管理员提交审批后，Controller 加载数据库原记录，设置 approverId（当前管理员 userId）、approveTime（当前时间）、approveRemark 和 status，保存更新。审批状态随后反馈至学生端 `/leave/my` 页面，学生可实时查看审批结果和审批备注。请假申请处理时序图如图4-3所示。

*（此处插入图4-3 请假申请处理时序图）*

#### 4.1.4 成绩录入与统计

管理员在 `/grade/add` 页面选择学生、课程、考试类型（期中考试/期末考试/平时成绩），输入成绩分值（0~100）和可选备注后提交。GradeController 首先校验分值是否在合法范围内，再调用 GradeService.isDuplicate 方法检查同一学生、同一课程、同一考试类型的记录是否已存在。若存在则返回错误提示；若不存在则创建 GradeInfo 实体，调用 fillRedundantFields 自动冗余学生和课程信息，通过 JPA 的 `@PrePersist` 生命周期回调自动计算 gradeLevel 和 status 字段后保存。

成绩统计模块通过 `/grade/statistics` 页面实现。管理员可按课程、班级、考试类型三个维度组合筛选。GradeService.getStatistics 方法从数据库加载全部成绩记录，依次应用筛选条件过滤，然后遍历计算以下统计指标：总记录数、平均分（保留一位小数）、最高分、最低分、及格率（分数 >= 60 的占比）、以及五个分数区间的分布（60分以下、60-69分、70-79分、80-89分、90-100分）。统计结果以 Map 形式返回 Controller，在前端通过四个统计卡片和五条 CSS 柱状条进行可视化展示。成绩管理流程图如图4-4所示。

*（此处插入图4-4 成绩管理流程图）*

#### 4.1.5 考勤管理

管理员在 `/attendance/add` 页面选择学生、课程、考勤日期和出勤状态（出勤/迟到/早退/缺勤/请假）后提交。AttendanceController 创建 AttendanceInfo 实体，调用 AttendanceService.fillRedundantFields 自动从 StudentInfo 和 CourseInfo 冗余填充关联信息后保存。编辑考勤时，Controller 先从数据库加载原记录，仅更新 attendanceDate、status 和 remark 三个字段，保留 studentId、courseId 等不可变字段不变，避免因表单 disabled 字段未提交而导致关联字段置空的问题。学生端 `/attendance/my` 根据 session 中的 relatedStudentId 仅查询和展示本人的考勤记录。

#### 4.1.6 核心类图

系统核心类设计如图4-5所示，展示了 entity 层 8 个实体类之间的 JPA 注解映射关系、Repository 层的继承结构、Service 层的关键业务方法以及 Controller 层与模板的映射关系。

*（此处插入图4-5 系统核心类图）*

### 4.2 界面的设计

系统前端采用 Thymeleaf 模板引擎进行服务端渲染，所有页面模板位于 `src/main/resources/templates/` 目录下，按模块分子目录组织。公共布局采用顶部导航栏 + 左侧菜单栏 + 右侧内容区的经典后台管理三段式布局。顶部导航栏固定高度 56px，深色背景（#2c3e50），展示系统标题、当前用户欢迎语、角色标签和退出按钮。左侧菜单栏固定宽度 200px，根据 session.role 动态渲染不同的菜单项分组；管理员菜单包含基础资料、业务管理和系统三个分组，涵盖全部管理模块入口；学生菜单包含个人信息、查询浏览、业务功能和系统四个分组，入口受限于本人数据范围。右侧内容区使用背景图叠加半透明遮罩，为系统增添校园氛围。

首页作为登录后的着陆页，右侧内容区展示毛玻璃效果欢迎卡片（background: rgba(255,255,255,0.36) + backdrop-filter: blur(9px)），卡片内展示欢迎标题、当前角色和问候语，同时保持顶部导航栏与左侧菜单栏的完整功能入口。登录页采用全屏校园背景图 + 半透明遮罩 + 偏右居中半透明白色登录卡片的设计。系统首页界面如图4-6所示，管理员成绩管理界面如图4-7所示。

*（此处插入图4-6 系统首页界面）*

*（此处插入图4-7 管理员成绩管理界面）*

## 5 实现与测试

### 5.1 编码

项目遵循 Maven 标准目录结构和 Spring Boot 推荐的分层包组织方式。Java 源代码位于 `src/main/java/com/example/student/` 目录下，包含以下子包：

- **config**：Spring 配置类，WebConfig 负责注册 LoginInterceptor 及排除公开路径
- **interceptor**：LoginInterceptor 实现 HandlerInterceptor 接口，对所有受保护请求进行 session 登录状态检查
- **entity**：8 个 JPA 实体类，使用 `@Entity`、`@Table`、`@Column`、`@Id`、`@GeneratedValue` 等 JPA 注解完成对象-关系映射，createTime 和 updateTime 通过 `@PrePersist`/`@PreUpdate` 生命周期回调自动维护，GradeInfo 实体额外在回调中自动计算 gradeLevel 和 status 字段
- **repository**：8 个 Spring Data JPA Repository 接口，继承 JpaRepository<T，Long>，通过方法命名约定定义自定义查询（如 findByStudentNo、findByStudentIdOrderByScoreDesc 等）
- **service**：9 个 Service 类，封装核心业务逻辑与事务边界。UserService 负责登录认证与注册事务（@Transactional 确保 student_info 与 user_account 同步插入），GradeService 提供成绩统计计算与学号重复性校验，CourseSelectionService、AttendanceService、LeaveService 各自提供冗余字段自动填充（fillRedundantFields）方法
- **controller**：9 个 Controller 类，使用 `@Controller` 和 `@RequestMapping` 注解，方法返回视图名称字符串或重定向指令。每个受保护的写操作路由均首先校验 session.role 是否为 "ADMIN"，非管理员访问时通过 RedirectAttributes 传递错误提示并重定向

表示层模板位于 `src/main/resources/templates/` 目录下，按 login.html、register.html、index.html（首页）以及 class/、course/、student/、selection/、attendance/、leave/、grade/、system/ 等模块子目录组织。静态资源（CSS 背景图等）位于 `src/main/resources/static/` 目录下。数据库初始化脚本 schema.sql 放置于 `src/main/resources/` 根目录，用户通过 Navicat 等客户端手动执行以完成建库建表及测试数据初始化。

命名规范方面，实体类名与数据库表名保持语义一致（如 ClassInfo ↔ class_info），Controller 类名采用"模块名 + Controller"格式，Service 类名采用"模块名 + Service"格式，方法名采用驼峰命名。Thymeleaf 模板文件名采用功能语义命名（如 list.html 为列表页，form.html 为表单页，my.html 为学生个人视图页）。

### 5.2 测试过程

#### 5.2.1 关键模块的单元测试

系统采取手动功能测试与代码逻辑复审相结合的方式，对以下关键模块进行了重点验证：

**用户注册与登录**：测试用例包括正常注册（新用户名 + 新学号）、用户名重复注册、学号重复注册、两次密码不一致、空字段提交等场景。验证注册成功后 student_info 和 user_account 同时插入且 related_student_id 绑定正确，登录成功后 session 中包含正确的 userId、username、role 和 relatedStudentId。

**成绩录入与等级计算**：测试用例覆盖各分值区间的等级判定：输入 95 分期望等级"优秀"、输入 85 分期望"良好"、输入 75 分期望"中等"、输入 65 分期望"及格"、输入 45 分期望"不及格"。测试分值边界：输入 0 分和 100 分可正常保存，输入 -1 或 101 分别触发应用层校验错误提示。测试重复录入：同一学生、同一课程、同一考试类型第二次保存时触发唯一性约束错误提示。

**学号唯一性校验**：新增模式下提交已存在的学号（如 "2024001"）期望提示"学号已存在"并返回表单。编辑模式下维持原学号不变期望正常保存；将学号改为其他已存在学号（如将王丰色的学号改为 2024001）期望提示重复且不保存。修复前的缺陷为编辑时未从数据库加载原记录进行比对，在边界情况下绕过了重复检查；修复后编辑分支先通过 findById 获取数据库原记录，仅在学号确实改变时才执行重复判断，currentId 取自数据库记录的 id。

#### 5.2.2 集成测试

系统进行了如下关键全流程集成测试：

**学生选课全流程**：注册新学生账号 → 登录 → 浏览可选课程列表 → 选择"高等数学" → 确认选课成功显示在"我的选课记录"中 → 再次选择同一课程提示"不能重复选课" → 在已选记录中点击"取消" → 选课记录消失，可选列表中该课程重新出现。管理员端同步验证该选课记录出现在 `/selection` 列表中。

**请假申请与审批全流程**：学生登录 → 提交请假申请（类型：病假，原因：身体不适，时间跨度为一天） → 学生端"我的请假"列表显示状态为"待审批" → 管理员登录 → 请假管理列表显示该条记录 → 点击审批 → 选择"已通过"并填写审批备注"已核实" → 提交 → 学生端刷新列表，状态更新为"已通过"，审批备注显示"已核实"。

**成绩统计联动**：管理员录入多名学生的多门课程成绩（覆盖不同考试类型和分数区间） → 访问成绩统计页面 → 默认显示全部数据的统计结果 → 按课程"高等数学"筛选，卡片和柱状图仅反映该课程数据 → 进一步按考试类型"期中考试"筛选，数据再次收窄 → 点击"重置"按钮恢复全部数据视图。

## 6 总结

本项目以"学生管理系统"为课程设计选题，基于 Java 8 + Spring Boot 2.7.18 + MySQL + Thymeleaf 技术栈，采用单体架构和事务脚本模式，完成了从需求分析、概要设计、数据库设计、详细设计到编码实现与测试的完整软件体系结构课程设计流程。

在需求分析阶段，明确了学生和管理员两类核心参与者及其用例边界，定义了注册登录、学生信息、班级信息、课程信息、选课、考勤、请假、成绩管理及成绩统计等九大功能模块。在概要设计阶段，确立了 MVC 三层分层架构，设计了业务架构、应用架构和技术架构，明确了各层的职责划分和交互方式。

在数据库设计阶段，完成了从 E-R 概念建模到 8 张核心数据表物理设计的完整过程。通过在选课、考勤、成绩等关联表中引入适当冗余字段，在查询效率与数据一致性之间取得了合理的平衡。在多表之间定义了外键约束与级联策略，在关键字段上设置了 UNIQUE 约束和 CHECK 约束，确保数据完整性。

在详细设计阶段，对登录认证、学生选课、请假审批、成绩录入与统计、考勤管理等核心业务流程进行了详细的时序分析和逻辑梳理，并通过编辑防 null、学号重复校验等实际问题的修复，深化了对应用层防御性编程重要性的理解。

在实现与测试阶段，遵循标准的 Spring Boot 项目结构和命名规范，通过单元测试用例和全流程集成测试验证了系统的功能正确性。测试过程中暴露出的学号重复校验漏洞（编辑场景下未从数据库加载原记录而仅依赖表单 id 值）以及静态资源被登录拦截器误拦截等问题，均在详细分析根因后得到了修复。

通过本次课程设计，深刻认识到软件体系结构设计对项目可维护性的决定性作用。清晰的 MVC 分层和事务脚本模式使得每个模块的职责边界明确，Bug 的定位和修复能够被限制在有限的几个类之中，不会波及整个项目。同时，JPA 实体生命周期回调（@PrePersist/@PreUpdate）在成绩等级自动计算中的应用，以及 Thymeleaf 服务端渲染在角色权限菜单动态展示中的应用，均体现了框架特性与业务需求相结合的设计价值。

## 参考文献

[1] 王珊，萨师煊.数据库系统概论[M].北京:高等教育出版社，2014.

[2] Craig Walls. Spring Boot in Action[M]. Manning Publications，2016.

[3] 徐立艳.计算机软件数据库设计的原则及问题研究[J].软件，2023，44(01):141-143.

[4] Spring Boot Reference Documentation[EB/OL]. https://docs.spring.io/spring-boot/docs/2.7.x/reference/html/.

[5] Thymeleaf Documentation[EB/OL]. https://www.thymeleaf.org/documentation.html.