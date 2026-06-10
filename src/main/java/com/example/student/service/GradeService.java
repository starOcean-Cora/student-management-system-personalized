package com.example.student.service;

import com.example.student.entity.CourseInfo;
import com.example.student.entity.GradeInfo;
import com.example.student.entity.StudentInfo;
import com.example.student.repository.CourseInfoRepository;
import com.example.student.repository.GradeInfoRepository;
import com.example.student.repository.StudentInfoRepository;
import org.springframework.stereotype.Service;

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

    public GradeInfo save(GradeInfo grade) {
        return gradeRepository.save(grade);
    }

    public void deleteById(Long id) {
        gradeRepository.deleteById(id);
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
}