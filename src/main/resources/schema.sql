-- ============================================================
-- 学生管理系统 — 数据库初始化脚本
-- 数据库名：student_management
-- 适用环境：本地实验演示（Navicat 手动执行）
-- ⚠️ 不要用于生产环境
-- ⚠️ 本 SQL 适合首次初始化数据库
--    如果重复执行，可能会因为用户名、学号、课程编号、选课记录、
--    成绩记录等唯一约束报错
--    重复执行前请先删除数据库或清空相关数据
-- ============================================================

-- 1. 建库
CREATE DATABASE IF NOT EXISTS student_management
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE student_management;

-- ============================================================
-- 2. 建表
-- ============================================================

-- 2.1 班级信息表
CREATE TABLE class_info (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    class_name  VARCHAR(100) NOT NULL                 COMMENT '班级名称',
    grade       VARCHAR(50)  DEFAULT NULL             COMMENT '年级',
    major       VARCHAR(100) DEFAULT NULL             COMMENT '专业',
    teacher_name VARCHAR(50) DEFAULT NULL             COMMENT '班主任/教师',
    remark      VARCHAR(500) DEFAULT NULL             COMMENT '备注',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级信息表';

-- 2.2 课程信息表
CREATE TABLE course_info (
    id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    course_code  VARCHAR(30)   NOT NULL                 COMMENT '课程编号',
    course_name  VARCHAR(100)  NOT NULL                 COMMENT '课程名称',
    teacher_name VARCHAR(50)   DEFAULT NULL             COMMENT '授课教师',
    credit       DECIMAL(3,1)  DEFAULT NULL             COMMENT '学分',
    class_hours  INT           DEFAULT NULL             COMMENT '课时',
    remark       VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_code (course_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程信息表';

-- 2.3 学生信息表
CREATE TABLE student_info (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    student_no  VARCHAR(30)  NOT NULL                 COMMENT '学号',
    name        VARCHAR(50)  NOT NULL                 COMMENT '姓名',
    gender      TINYINT      DEFAULT NULL             COMMENT '性别（0=女, 1=男）',
    class_id    BIGINT       DEFAULT NULL             COMMENT '班级ID',
    class_name  VARCHAR(100) DEFAULT NULL             COMMENT '班级名称（冗余）',
    phone       VARCHAR(20)  DEFAULT NULL             COMMENT '手机号',
    email       VARCHAR(100) DEFAULT NULL             COMMENT '邮箱',
    avatar      VARCHAR(255) DEFAULT NULL             COMMENT '头像路径',
    status      TINYINT      NOT NULL DEFAULT 1       COMMENT '状态（1=在读, 0=离校）',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no),
    KEY idx_class_id (class_id),
    CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class_info (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- 2.4 用户账号表
CREATE TABLE user_account (
    id                  BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    username            VARCHAR(50)  NOT NULL                 COMMENT '用户名',
    password            VARCHAR(100) NOT NULL                 COMMENT '密码',
    role                VARCHAR(20)  NOT NULL                 COMMENT '角色（STUDENT/ADMIN）',
    related_student_id  BIGINT       DEFAULT NULL             COMMENT '关联学生ID（管理员为NULL）',
    avatar              VARCHAR(255) DEFAULT NULL             COMMENT '头像路径',
    status              TINYINT      NOT NULL DEFAULT 1       COMMENT '状态（1=启用, 0=禁用）',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_related_student_id (related_student_id),
    CONSTRAINT fk_user_student FOREIGN KEY (related_student_id) REFERENCES student_info (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号表';

-- 2.5 选课信息表
CREATE TABLE course_selection (
    id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    student_id   BIGINT       NOT NULL                 COMMENT '学生ID',
    student_no   VARCHAR(30)  NOT NULL                 COMMENT '学号（冗余）',
    student_name VARCHAR(50)  NOT NULL                 COMMENT '学生姓名（冗余）',
    class_id     BIGINT       DEFAULT NULL             COMMENT '班级ID',
    class_name   VARCHAR(100) DEFAULT NULL             COMMENT '班级名称（冗余）',
    course_id    BIGINT       NOT NULL                 COMMENT '课程ID',
    course_name  VARCHAR(100) NOT NULL                 COMMENT '课程名称（冗余）',
    select_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    status       VARCHAR(20)  NOT NULL DEFAULT '已选'  COMMENT '选课状态（已选/退选）',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_course (student_id, course_id),
    KEY idx_student_id (student_id),
    KEY idx_course_id (course_id),
    KEY idx_class_id (class_id),
    CONSTRAINT fk_selection_student FOREIGN KEY (student_id) REFERENCES student_info (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_selection_course  FOREIGN KEY (course_id)  REFERENCES course_info (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_selection_class   FOREIGN KEY (class_id)   REFERENCES class_info (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课信息表';

-- 2.6 考勤信息表
CREATE TABLE attendance_info (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    student_id      BIGINT       NOT NULL                 COMMENT '学生ID',
    student_no      VARCHAR(30)  NOT NULL                 COMMENT '学号（冗余）',
    student_name    VARCHAR(50)  NOT NULL                 COMMENT '学生姓名（冗余）',
    class_id        BIGINT       DEFAULT NULL             COMMENT '班级ID',
    class_name      VARCHAR(100) DEFAULT NULL             COMMENT '班级名称（冗余）',
    course_id       BIGINT       NOT NULL                 COMMENT '课程ID',
    course_name     VARCHAR(100) NOT NULL                 COMMENT '课程名称（冗余）',
    attendance_date DATE         NOT NULL                 COMMENT '考勤日期',
    status          VARCHAR(20)  NOT NULL                 COMMENT '考勤状态（出勤/迟到/早退/缺勤/请假）',
    remark          VARCHAR(500) DEFAULT NULL             COMMENT '备注',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_student_id (student_id),
    KEY idx_course_id (course_id),
    KEY idx_class_id (class_id),
    KEY idx_attendance_date (attendance_date),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES student_info (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_attendance_course  FOREIGN KEY (course_id)  REFERENCES course_info (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_attendance_class   FOREIGN KEY (class_id)   REFERENCES class_info (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤信息表';

-- 2.7 请假信息表
CREATE TABLE leave_info (
    id             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    student_id     BIGINT       NOT NULL                 COMMENT '学生ID',
    student_no     VARCHAR(30)  NOT NULL                 COMMENT '学号（冗余）',
    student_name   VARCHAR(50)  NOT NULL                 COMMENT '学生姓名（冗余）',
    class_id       BIGINT       DEFAULT NULL             COMMENT '班级ID',
    class_name     VARCHAR(100) DEFAULT NULL             COMMENT '班级名称（冗余）',
    leave_type     VARCHAR(20)  NOT NULL                 COMMENT '请假类型（事假/病假/其他）',
    reason         VARCHAR(500) NOT NULL                 COMMENT '请假原因',
    start_time     DATETIME     NOT NULL                 COMMENT '开始时间',
    end_time       DATETIME     NOT NULL                 COMMENT '结束时间',
    status         VARCHAR(20)  NOT NULL DEFAULT '待审批' COMMENT '审批状态（待审批/已通过/已驳回）',
    approver_id    BIGINT       DEFAULT NULL             COMMENT '审批人ID（管理员）',
    approve_time   DATETIME     DEFAULT NULL             COMMENT '审批时间',
    approve_remark VARCHAR(500) DEFAULT NULL             COMMENT '审批备注（审批人填写）',
    remark         VARCHAR(500) DEFAULT NULL             COMMENT '学生备注（学生申请时填写）',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    update_time    DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_student_id (student_id),
    KEY idx_class_id (class_id),
    KEY idx_approver_id (approver_id),
    KEY idx_status (status),
    CONSTRAINT fk_leave_student  FOREIGN KEY (student_id)  REFERENCES student_info (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_leave_class    FOREIGN KEY (class_id)    REFERENCES class_info (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_leave_approver FOREIGN KEY (approver_id) REFERENCES user_account (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假信息表';

-- 2.8 成绩信息表
-- ⚠️ score 的 0~100 校验由 CHECK 约束 + 应用层双重保障
CREATE TABLE grade_info (
    id           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    student_id   BIGINT        NOT NULL                 COMMENT '学生ID',
    student_no   VARCHAR(30)   NOT NULL                 COMMENT '学号（冗余）',
    student_name VARCHAR(50)   NOT NULL                 COMMENT '学生姓名（冗余）',
    class_id     BIGINT        DEFAULT NULL             COMMENT '班级ID',
    class_name   VARCHAR(100)  DEFAULT NULL             COMMENT '班级名称（冗余）',
    course_id    BIGINT        NOT NULL                 COMMENT '课程ID',
    course_name  VARCHAR(100)  NOT NULL                 COMMENT '课程名称（冗余）',
    exam_type    VARCHAR(20)   NOT NULL                 COMMENT '考试类型（期中考试/期末考试/平时成绩）',
    score        DECIMAL(5,1)  NOT NULL                 COMMENT '成绩',
    grade_level  VARCHAR(20)   NOT NULL                 COMMENT '等级（优秀/良好/中等/及格/不及格）',
    status       VARCHAR(20)   NOT NULL DEFAULT '已录入' COMMENT '状态（已录入/需关注）',
    remark       VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    update_time  DATETIME      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_course_exam (student_id, course_id, exam_type),
    KEY idx_student_id (student_id),
    KEY idx_course_id (course_id),
    KEY idx_class_id (class_id),
    KEY idx_exam_type (exam_type),
    KEY idx_score (score),
    CONSTRAINT chk_score_range CHECK (score >= 0 AND score <= 100),
    CONSTRAINT fk_grade_student FOREIGN KEY (student_id) REFERENCES student_info (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_grade_course  FOREIGN KEY (course_id)  REFERENCES course_info (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_grade_class   FOREIGN KEY (class_id)   REFERENCES class_info (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩信息表';

-- ============================================================
-- 3. 初始化管理员账号
-- ⚠️ 默认账号 admin / admin123 仅用于本地实验演示
--    README 中需提醒不要用于生产环境
-- ============================================================
INSERT INTO user_account (username, password, role, related_student_id, status, create_time)
VALUES ('admin', 'admin123', 'ADMIN', NULL, 1, NOW());

-- ============================================================
-- 4. 测试数据
-- ============================================================

-- 4.1 班级
INSERT INTO class_info (class_name, grade, major, teacher_name) VALUES
('计算机科学2024-1班', '2024级', '计算机科学与技术', '殷老师'),
('软件工程2024-1班', '2024级', '软件工程', '郭老师');

-- 4.2 课程
INSERT INTO course_info (course_code, course_name, teacher_name, credit, class_hours) VALUES
('CS101', '高等数学', '柯教授', 4.0, 64),
('CS102', 'Java程序设计', '史老师', 3.0, 48),
('CS103', '数据库原理', '钱老师', 3.0, 48);

-- 4.3 学生
INSERT INTO student_info (student_no, name, gender, class_id, class_name, phone, email) VALUES
('2024001', '张三', 1, 1, '计算机科学2024-1班', '13800000001', 'zhangsan@example.com'),
('2024002', '李四', 0, 1, '计算机科学2024-1班', '13800000002', 'lisi@example.com'),
('2024003', '王五', 1, 2, '软件工程2024-1班', '13800000003', 'wangwu@example.com');

-- 4.4 学生用户账号
INSERT INTO user_account (username, password, role, related_student_id, status, create_time) VALUES
('zhangsan', '123456', 'STUDENT', 1, 1, NOW()),
('lisi', '123456', 'STUDENT', 2, 1, NOW()),
('wangwu', '123456', 'STUDENT', 3, 1, NOW());

-- 4.5 选课记录
INSERT INTO course_selection (student_id, student_no, student_name, class_id, class_name, course_id, course_name, select_time, status) VALUES
(1, '2024001', '张三', 1, '计算机科学2024-1班', 1, '高等数学', NOW(), '已选'),
(1, '2024001', '张三', 1, '计算机科学2024-1班', 2, 'Java程序设计', NOW(), '已选'),
(2, '2024002', '李四', 1, '计算机科学2024-1班', 1, '高等数学', NOW(), '已选'),
(3, '2024003', '王五', 2, '软件工程2024-1班', 3, '数据库原理', NOW(), '已选');

-- 4.6 成绩（覆盖期中/期末/平时，含不及格数据）
INSERT INTO grade_info (student_id, student_no, student_name, class_id, class_name, course_id, course_name, exam_type, score, grade_level, status, remark) VALUES
(1, '2024001', '张三', 1, '计算机科学2024-1班', 1, '高等数学', '期中考试', 92.0, '优秀', '已录入', ''),
(1, '2024001', '张三', 1, '计算机科学2024-1班', 2, 'Java程序设计', '期末考试', 85.5, '良好', '已录入', ''),
(2, '2024002', '李四', 1, '计算机科学2024-1班', 1, '高等数学', '期中考试', 58.0, '不及格', '需关注', ''),
(3, '2024003', '王五', 2, '软件工程2024-1班', 1, '高等数学', '期末考试', 76.0, '中等', '已录入', ''),
(3, '2024003', '王五', 2, '软件工程2024-1班', 3, '数据库原理', '平时成绩', 45.0, '不及格', '需关注', '需补考');

-- 4.7 考勤
INSERT INTO attendance_info (student_id, student_no, student_name, class_id, class_name, course_id, course_name, attendance_date, status) VALUES
(1, '2024001', '张三', 1, '计算机科学2024-1班', 1, '高等数学', '2026-06-01', '出勤'),
(2, '2024002', '李四', 1, '计算机科学2024-1班', 1, '高等数学', '2026-06-01', '迟到');

-- 4.8 请假
INSERT INTO leave_info (student_id, student_no, student_name, class_id, class_name, leave_type, reason, start_time, end_time, status, remark) VALUES
(1, '2024001', '张三', 1, '计算机科学2024-1班', '病假', '身体不适', '2026-06-02 08:00:00', '2026-06-02 17:00:00', '待审批', '已提交医院证明');

-- ============================================================
-- 脚本结束
-- ============================================================
