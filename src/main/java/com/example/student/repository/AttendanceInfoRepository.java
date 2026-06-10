package com.example.student.repository;

import com.example.student.entity.AttendanceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceInfoRepository extends JpaRepository<AttendanceInfo, Long> {
    List<AttendanceInfo> findByStudentIdOrderByAttendanceDateDesc(Long studentId);
}