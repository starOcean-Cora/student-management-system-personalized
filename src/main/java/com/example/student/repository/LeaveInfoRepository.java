package com.example.student.repository;

import com.example.student.entity.LeaveInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveInfoRepository extends JpaRepository<LeaveInfo, Long> {
    List<LeaveInfo> findByStudentIdOrderByCreateTimeDesc(Long studentId);

    @Query("select l from LeaveInfo l " +
            "where (:leaveType is null or :leaveType = '' or l.leaveType like concat('%', :leaveType, '%')) " +
            "and (:status is null or :status = '' or l.status = :status)")
    Page<LeaveInfo> findAdminPage(@Param("leaveType") String leaveType,
                                  @Param("status") String status,
                                  Pageable pageable);

    @Query("select l from LeaveInfo l " +
            "where l.studentId = :studentId " +
            "and (:leaveType is null or :leaveType = '' or l.leaveType like concat('%', :leaveType, '%')) " +
            "and (:status is null or :status = '' or l.status = :status)")
    Page<LeaveInfo> findStudentPage(@Param("studentId") Long studentId,
                                    @Param("leaveType") String leaveType,
                                    @Param("status") String status,
                                    Pageable pageable);
}
