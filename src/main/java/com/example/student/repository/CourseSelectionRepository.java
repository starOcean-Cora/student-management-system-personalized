package com.example.student.repository;

import com.example.student.entity.CourseSelection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {
    List<CourseSelection> findByStudentId(Long studentId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    CourseSelection findByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query("select s from CourseSelection s " +
            "where (:studentName is null or :studentName = '' or s.studentName like concat('%', :studentName, '%')) " +
            "and (:courseId is null or s.courseId = :courseId)")
    Page<CourseSelection> findAdminPage(@Param("studentName") String studentName,
                                        @Param("courseId") Long courseId,
                                        Pageable pageable);

    @Query("select s from CourseSelection s " +
            "where s.studentId = :studentId " +
            "and (:courseName is null or :courseName = '' or s.courseName like concat('%', :courseName, '%')) " +
            "and (:courseId is null or s.courseId = :courseId)")
    Page<CourseSelection> findStudentPage(@Param("studentId") Long studentId,
                                          @Param("courseName") String courseName,
                                          @Param("courseId") Long courseId,
                                          Pageable pageable);
}
