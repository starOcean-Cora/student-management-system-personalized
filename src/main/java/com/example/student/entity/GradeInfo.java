package com.example.student.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grade_info")
public class GradeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "student_no", nullable = false, length = 30)
    private String studentNo;

    @Column(name = "student_name", nullable = false, length = 50)
    private String studentName;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "class_name", length = 100)
    private String className;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Column(name = "exam_type", nullable = false, length = 20)
    private String examType;

    @Column(nullable = false)
    private Double score;

    @Column(name = "grade_level", nullable = false, length = 20)
    private String gradeLevel;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 500)
    private String remark;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        computeGradeAndStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
        computeGradeAndStatus();
    }

    public void computeGradeAndStatus() {
        if (score == null) return;
        if (score >= 90) gradeLevel = "优秀";
        else if (score >= 80) gradeLevel = "良好";
        else if (score >= 70) gradeLevel = "中等";
        else if (score >= 60) gradeLevel = "及格";
        else gradeLevel = "不及格";
        status = score >= 60 ? "已录入" : "需关注";
    }

    public GradeInfo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}