package com.example.student.repository;

import com.example.student.entity.GradeInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeInfoRepository extends JpaRepository<GradeInfo, Long> {

    @Query("SELECT g FROM GradeInfo g ORDER BY g.score DESC, g.id ASC")
    List<GradeInfo> findAllOrderByScoreDesc();

    List<GradeInfo> findByStudentIdOrderByScoreDesc(Long studentId);

    @Query("SELECT g FROM GradeInfo g " +
            "WHERE (:courseName = '' OR g.courseName LIKE CONCAT('%', :courseName, '%')) " +
            "AND (:examType = '' OR g.examType = :examType) " +
            "ORDER BY g.score DESC, g.id ASC")
    Page<GradeInfo> findAdminPage(@Param("courseName") String courseName,
                                  @Param("examType") String examType,
                                  Pageable pageable);

    @Query("SELECT g FROM GradeInfo g " +
            "WHERE g.studentId = :studentId " +
            "AND (:courseName = '' OR g.courseName LIKE CONCAT('%', :courseName, '%')) " +
            "AND (:examType = '' OR g.examType = :examType) " +
            "ORDER BY g.score DESC, g.id ASC")
    Page<GradeInfo> findStudentPage(@Param("studentId") Long studentId,
                                    @Param("courseName") String courseName,
                                    @Param("examType") String examType,
                                    Pageable pageable);

    boolean existsByStudentIdAndCourseIdAndExamType(Long studentId, Long courseId, String examType);

    boolean existsByStudentIdAndCourseIdAndExamTypeAndIdNot(Long studentId, Long courseId, String examType, Long id);
}
