package com.example.student.service;

import com.example.student.entity.CourseInfo;
import com.example.student.entity.GradeInfo;
import com.example.student.entity.StudentInfo;
import com.example.student.repository.CourseInfoRepository;
import com.example.student.repository.GradeInfoRepository;
import com.example.student.repository.StudentInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

import java.util.List;
import java.util.Optional;

@Service
public class GradeService {

    private final GradeInfoRepository gradeRepository;
    private final StudentInfoRepository studentInfoRepository;
    private final CourseInfoRepository courseInfoRepository;

    public GradeService(GradeInfoRepository gradeRepository,
                        StudentInfoRepository studentInfoRepository,
                        CourseInfoRepository courseInfoRepository) {
        this.gradeRepository = gradeRepository;
        this.studentInfoRepository = studentInfoRepository;
        this.courseInfoRepository = courseInfoRepository;
    }

    public List<GradeInfo> findAllOrderByScoreDesc() {
        return gradeRepository.findAllOrderByScoreDesc();
    }

    public Optional<GradeInfo> findById(Long id) {
        return gradeRepository.findById(id);
    }

    public List<GradeInfo> findByStudentId(Long studentId) {
        return gradeRepository.findByStudentIdOrderByScoreDesc(studentId);
    }

    public Page<GradeInfo> findAdminPage(String courseName, String examType, Pageable pageable) {
        return gradeRepository.findAdminPage(courseName, examType, pageable);
    }

    public Page<GradeInfo> findStudentPage(Long studentId, String courseName, String examType, Pageable pageable) {
        return gradeRepository.findStudentPage(studentId, courseName, examType, pageable);
    }

    public GradeInfo save(GradeInfo grade) {
        return gradeRepository.save(grade);
    }

    public void deleteById(Long id) {
        gradeRepository.deleteById(id);
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
            if (!gradeRepository.existsById(id)) {
                throw new IllegalArgumentException("record not found: " + id);
            }
        }
        for (Long id : uniqueIds) {
            deleteById(id);
        }
        return uniqueIds.size();
    }

    public boolean isDuplicate(Long studentId, Long courseId, String examType) {
        return gradeRepository.existsByStudentIdAndCourseIdAndExamType(studentId, courseId, examType);
    }

    public boolean isDuplicateExcept(Long studentId, Long courseId, String examType, Long excludeId) {
        return gradeRepository.existsByStudentIdAndCourseIdAndExamTypeAndIdNot(studentId, courseId, examType, excludeId);
    }

    public void fillRedundantFields(GradeInfo grade) {
        if (grade.getStudentId() != null) {
            StudentInfo student = studentInfoRepository.findById(grade.getStudentId()).orElse(null);
            if (student != null) {
                grade.setStudentNo(student.getStudentNo());
                grade.setStudentName(student.getName());
                grade.setClassId(student.getClassId());
                grade.setClassName(student.getClassName());
            }
        }
        if (grade.getCourseId() != null) {
            CourseInfo course = courseInfoRepository.findById(grade.getCourseId()).orElse(null);
            if (course != null) {
                grade.setCourseName(course.getCourseName());
            }
        }
    }
    public Map<String, Object> getStatistics(Long courseId, Long classId, String examType) {
        List<GradeInfo> all = gradeRepository.findAll();
        List<GradeInfo> filtered = new ArrayList<>();

        for (GradeInfo g : all) {
            if (courseId != null && !courseId.equals(g.getCourseId())) continue;
            if (classId != null && !classId.equals(g.getClassId())) continue;
            if (examType != null && !examType.isEmpty() && !examType.equals(g.getExamType())) continue;
            filtered.add(g);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("count", filtered.size());

        if (filtered.isEmpty()) {
            stats.put("avg", null);
            stats.put("max", null);
            stats.put("min", null);
            stats.put("passRate", 0.0);
            stats.put("range1", 0); // <60
            stats.put("range2", 0); // 60-69
            stats.put("range3", 0); // 70-79
            stats.put("range4", 0); // 80-89
            stats.put("range5", 0); // 90-100
            stats.put("maxRange", 0);
            return stats;
        }

        double sum = 0, max = Double.MIN_VALUE, min = Double.MAX_VALUE;
        int passCount = 0;
        int r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0;

        for (GradeInfo g : filtered) {
            double s = g.getScore();
            sum += s;
            if (s > max) max = s;
            if (s < min) min = s;
            if (s >= 60) passCount++;

            if (s < 60) r1++;
            else if (s < 70) r2++;
            else if (s < 80) r3++;
            else if (s < 90) r4++;
            else r5++;
        }

        stats.put("avg", Math.round(sum / filtered.size() * 10.0) / 10.0);
        stats.put("max", max);
        stats.put("min", min);
        stats.put("passRate", Math.round(passCount * 1000.0 / filtered.size()) / 10.0);
        stats.put("range1", r1);
        stats.put("range2", r2);
        stats.put("range3", r3);
        stats.put("range4", r4);
        stats.put("range5", r5);
        stats.put("maxRange", (int)Math.max(r1, Math.max(r2, Math.max(r3, Math.max(r4, r5)))));

        return stats;
    }
}
