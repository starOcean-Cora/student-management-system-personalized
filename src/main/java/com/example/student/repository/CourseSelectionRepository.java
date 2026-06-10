package com.example.student.repository;

import com.example.student.entity.CourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {
    List<CourseSelection> findByStudentId(Long studentId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    CourseSelection findByStudentIdAndCourseId(Long studentId, Long courseId);
}