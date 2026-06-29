package com.example.student.service;

import com.example.student.entity.StudentInfo;
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

    public Page<StudentInfo> findAdminPage(String name, Long classId, Pageable pageable) {
        String keyword = name == null ? "" : name.trim();
        return studentInfoRepository.findAdminPage(keyword, classId, pageable);
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
            if (!studentInfoRepository.existsById(id)) {
                throw new IllegalArgumentException("record not found: " + id);
            }
        }
        for (Long id : uniqueIds) {
            deleteById(id);
        }
        return uniqueIds.size();
    }

    /**
     * Check if studentNo already belongs to a different student.
     * @param studentNo the student number to check
     * @param currentId the id of the student being edited, null for new students
     * @return true if duplicate (exists on another record), false if available
     */
    public boolean isStudentNoDuplicate(String studentNo, Long currentId) {
        StudentInfo existingByNo = studentInfoRepository.findByStudentNo(studentNo);
        if (existingByNo == null) return false;
        if (currentId == null) return true;
        return !existingByNo.getId().equals(currentId);
    }
}
