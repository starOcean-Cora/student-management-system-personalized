package com.example.student.repository;

import com.example.student.entity.AttendanceInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceInfoRepository extends JpaRepository<AttendanceInfo, Long> {
    List<AttendanceInfo> findByStudentIdOrderByAttendanceDateDesc(Long studentId);

    @Query("select a from AttendanceInfo a " +
            "where (:studentNo is null or :studentNo = '' or a.studentNo like concat('%', :studentNo, '%')) " +
            "and (:studentId is null or a.studentId = :studentId)")
    Page<AttendanceInfo> findAdminPage(@Param("studentNo") String studentNo,
                                       @Param("studentId") Long studentId,
                                       Pageable pageable);

    @Query("select a from AttendanceInfo a " +
            "where a.studentId = :studentId " +
            "and (:courseName is null or :courseName = '' or a.courseName like concat('%', :courseName, '%')) " +
            "and (:courseId is null or a.courseId = :courseId)")
    Page<AttendanceInfo> findStudentPage(@Param("studentId") Long studentId,
                                         @Param("courseName") String courseName,
                                         @Param("courseId") Long courseId,
                                         Pageable pageable);
}
