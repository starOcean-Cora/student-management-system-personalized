package com.example.student.service;

import com.example.student.entity.LeaveInfo;
import com.example.student.entity.StudentInfo;
import com.example.student.repository.LeaveInfoRepository;
import com.example.student.repository.StudentInfoRepository;
import org.springframework.stereotype.Service;

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

    public Optional<LeaveInfo> findById(Long id) {
        return leaveRepository.findById(id);
    }

    public List<LeaveInfo> findByStudentId(Long studentId) {
        return leaveRepository.findByStudentIdOrderByCreateTimeDesc(studentId);
    }

    public LeaveInfo save(LeaveInfo leave) {
        return leaveRepository.save(leave);
    }

    public void deleteById(Long id) {
        leaveRepository.deleteById(id);
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