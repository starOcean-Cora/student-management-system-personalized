package com.example.student.repository;

import com.example.student.entity.StudentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentInfoRepository extends JpaRepository<StudentInfo, Long> {
    boolean existsByStudentNo(String studentNo);
    StudentInfo findByStudentNo(String studentNo);
}