package com.example.student.service;

import com.example.student.entity.CourseInfo;
import com.example.student.entity.CourseSelection;
import com.example.student.entity.StudentInfo;
import com.example.student.repository.CourseInfoRepository;
import com.example.student.repository.CourseSelectionRepository;
import com.example.student.repository.StudentInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Service
public class CourseSelectionService {

    private final CourseSelectionRepository selectionRepository;
    private final StudentInfoRepository studentInfoRepository;
    private final CourseInfoRepository courseInfoRepository;

    public CourseSelectionService(CourseSelectionRepository selectionRepository,
                                  StudentInfoRepository studentInfoRepository,
                                  CourseInfoRepository courseInfoRepository) {
        this.selectionRepository = selectionRepository;
        this.studentInfoRepository = studentInfoRepository;
        this.courseInfoRepository = courseInfoRepository;
    }

    public List<CourseSelection> findAll() {
        return selectionRepository.findAll();
    }

    public Page<CourseSelection> findAdminPage(String studentName, Long courseId, Pageable pageable) {
        String keyword = studentName == null ? "" : studentName.trim();
        return selectionRepository.findAdminPage(keyword, courseId, pageable);
    }

    public Optional<CourseSelection> findById(Long id) {
        return selectionRepository.findById(id);
    }

    public CourseSelection save(CourseSelection selection) {
        return selectionRepository.save(selection);
    }

    public void deleteById(Long id) {
        selectionRepository.deleteById(id);
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
            if (!selectionRepository.existsById(id)) {
                throw new IllegalArgumentException("record not found: " + id);
            }
        }
        for (Long id : uniqueIds) {
            deleteById(id);
        }
        return uniqueIds.size();
    }

    public List<CourseSelection> findByStudentId(Long studentId) {
        return selectionRepository.findByStudentId(studentId);
    }

    public Page<CourseSelection> findStudentPage(Long studentId, String courseName, Long courseId, Pageable pageable) {
        String keyword = courseName == null ? "" : courseName.trim();
        return selectionRepository.findStudentPage(studentId, keyword, courseId, pageable);
    }

    public boolean isAlreadySelected(Long studentId, Long courseId) {
        return selectionRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    public CourseSelection selectCourse(Long studentId, Long courseId) {
        StudentInfo student = studentInfoRepository.findById(studentId).orElse(null);
        CourseInfo course = courseInfoRepository.findById(courseId).orElse(null);
        if (student == null || course == null) return null;
        CourseSelection cs = new CourseSelection();
        cs.setStudentId(student.getId());
        cs.setStudentNo(student.getStudentNo());
        cs.setStudentName(student.getName());
        cs.setClassId(student.getClassId());
        cs.setClassName(student.getClassName());
        cs.setCourseId(course.getId());
        cs.setCourseName(course.getCourseName());
        cs.setSelectTime(LocalDateTime.now());
        cs.setStatus("已选");
        cs.setCreateTime(LocalDateTime.now());
        return selectionRepository.save(cs);
    }

    // Sync class info: used when student class info is updated
    public int syncStudentClassInfo(Long studentId) {
        StudentInfo student = studentInfoRepository.findById(studentId).orElse(null);
        if (student == null || student.getClassId() == null) return 0;
        List<CourseSelection> selections = selectionRepository.findByStudentId(studentId);
        int count = 0;
        for (CourseSelection s : selections) {
            if (s.getClassId() == null || !s.getClassId().equals(student.getClassId())
                || s.getClassName() == null || !s.getClassName().equals(student.getClassName())) {
                s.setClassId(student.getClassId());
                s.setClassName(student.getClassName());
                selectionRepository.save(s);
                count++;
            }
        }
        return count;
    }

    // Repair: fill null class info for ALL existing selections retroactively
    public int repairAllNullClasses() {
        List<CourseSelection> all = selectionRepository.findAll();
        int count = 0;
        for (CourseSelection s : all) {
            if (s.getClassName() == null || s.getClassName().isEmpty()) {
                StudentInfo student = studentInfoRepository.findById(s.getStudentId()).orElse(null);
                if (student != null && student.getClassId() != null) {
                    s.setClassId(student.getClassId());
                    s.setClassName(student.getClassName());
                    selectionRepository.save(s);
                    count++;
                }
            }
        }
        return count;
    }
}