package com.example.student.service;

import com.example.student.entity.StudentInfo;
import com.example.student.repository.StudentInfoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentInfoRepository studentInfoRepository;
    private final CourseSelectionService courseSelectionService;

    public StudentService(StudentInfoRepository studentInfoRepository,
                           CourseSelectionService courseSelectionService) {
        this.studentInfoRepository = studentInfoRepository;
        this.courseSelectionService = courseSelectionService;
    }

    public List<StudentInfo> findAll() {
        return studentInfoRepository.findAll();
    }

    public Optional<StudentInfo> findById(Long id) {
        return studentInfoRepository.findById(id);
    }

    public StudentInfo save(StudentInfo studentInfo) {
        StudentInfo saved = studentInfoRepository.save(studentInfo);
        // Sync class info to existing selection records using entity-level update
        if (studentInfo.getId() != null && studentInfo.getClassId() != null) {
            courseSelectionService.syncStudentClassInfo(studentInfo.getId());
        }
        return saved;
    }

    public void deleteById(Long id) {
        studentInfoRepository.deleteById(id);
    }

    public boolean isStudentNoDuplicate(String studentNo, Long excludeId) {
        StudentInfo existing = studentInfoRepository.findByStudentNo(studentNo);
        if (existing == null) return false;
        return !existing.getId().equals(excludeId);
    }
}