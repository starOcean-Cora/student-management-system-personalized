package com.example.student.repository;

import com.example.student.entity.GradeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeInfoRepository extends JpaRepository<GradeInfo, Long> {

    @Query("SELECT g FROM GradeInfo g ORDER BY g.score DESC, g.id ASC")
    List<GradeInfo> findAllOrderByScoreDesc();

    List<GradeInfo> findByStudentIdOrderByScoreDesc(Long studentId);

    boolean existsByStudentIdAndCourseIdAndExamType(Long studentId, Long courseId, String examType);

    boolean existsByStudentIdAndCourseIdAndExamTypeAndIdNot(Long studentId, Long courseId, String examType, Long id);
}