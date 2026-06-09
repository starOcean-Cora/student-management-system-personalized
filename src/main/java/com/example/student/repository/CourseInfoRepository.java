package com.example.student.repository;

import com.example.student.entity.CourseInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseInfoRepository extends JpaRepository<CourseInfo, Long> {
    boolean existsByCourseCode(String courseCode);
    CourseInfo findByCourseCode(String courseCode);
}