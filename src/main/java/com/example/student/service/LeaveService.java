package com.example.student.service;

import com.example.student.entity.LeaveInfo;
import com.example.student.entity.StudentInfo;
import com.example.student.repository.LeaveInfoRepository;
import com.example.student.repository.StudentInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveService {

    private final LeaveInfoRepository leaveRepository;
    private final StudentInfoRepository studentInfoRepository;

    public LeaveService(LeaveInfoRepository leaveRepository,
                        StudentInfoRepository studentInfoRepository) {
        this.leaveRepository = leaveRepository;
        this.studentInfoRepository = studentInfoRepository;
    }

    public List<LeaveInfo> findAll() {
        return leaveRepository.findAll();
    }

    public Page<LeaveInfo> findAdminPage(String leaveType, String status, Pageable pageable) {
        String typeKeyword = leaveType == null ? "" : leaveType.trim();
        String selectedStatus = status == null ? "" : status.trim();
        return leaveRepository.findAdminPage(typeKeyword, selectedStatus, pageable);
    }

    public Optional<LeaveInfo> findById(Long id) {
        return leaveRepository.findById(id);
    }

    public List<LeaveInfo> findByStudentId(Long studentId) {
        return leaveRepository.findByStudentIdOrderByCreateTimeDesc(studentId);
    }

    public Page<LeaveInfo> findStudentPage(Long studentId, String leaveType, String status, Pageable pageable) {
        String typeKeyword = leaveType == null ? "" : leaveType.trim();
        String selectedStatus = status == null ? "" : status.trim();
        return leaveRepository.findStudentPage(studentId, typeKeyword, selectedStatus, pageable);
    }

    public LeaveInfo save(LeaveInfo leave) {
        return leaveRepository.save(leave);
    }

    public void deleteById(Long id) {
        leaveRepository.deleteById(id);
    }

    @Transactional
    public int batchDeleteByIds(List<Long> ids) {
        if (ids == null) {
            return 0;
        }
        List<Long> uniqueIds = new ArrayList<>(new LinkedHashSet<>(ids));
        uniqueIds.removeIf(id -> id == null);
        if (uniqueIds.isEmpty()) {
            return 0;
        }
        for (Long id : uniqueIds) {
            if (!leaveRepository.existsById(id)) {
                throw new IllegalArgumentException("record not found: " + id);
            }
        }
        for (Long id : uniqueIds) {
            deleteById(id);
        }
        return uniqueIds.size();
    }

    public void fillRedundantFields(LeaveInfo leave) {
        if (leave.getStudentId() != null) {
            StudentInfo student = studentInfoRepository.findById(leave.getStudentId()).orElse(null);
            if (student != null) {
                leave.setStudentNo(student.getStudentNo());
                leave.setStudentName(student.getName());
                leave.setClassId(student.getClassId());
                leave.setClassName(student.getClassName());
            }
        }
    }
}