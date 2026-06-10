package com.example.student.repository;

import com.example.student.entity.LeaveInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveInfoRepository extends JpaRepository<LeaveInfo, Long> {
    List<LeaveInfo> findByStudentIdOrderByCreateTimeDesc(Long studentId);
}