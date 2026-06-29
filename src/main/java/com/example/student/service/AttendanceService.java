package com.example.student.service;

import com.example.student.entity.AttendanceInfo;
import com.example.student.entity.CourseInfo;
import com.example.student.entity.StudentInfo;
import com.example.student.repository.AttendanceInfoRepository;
import com.example.student.repository.CourseInfoRepository;
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
public class AttendanceService {

    private final AttendanceInfoRepository attendanceRepository;
    private final StudentInfoRepository studentInfoRepository;
    private final CourseInfoRepository courseInfoRepository;

    public AttendanceService(AttendanceInfoRepository attendanceRepository,
                             StudentInfoRepository studentInfoRepository,
                             CourseInfoRepository courseInfoRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentInfoRepository = studentInfoRepository;
        this.courseInfoRepository = courseInfoRepository;
    }

    public List<AttendanceInfo> findAll() {
        return attendanceRepository.findAll();
    }

    public Page<AttendanceInfo> findAdminPage(String studentNo, Long studentId, Pageable pageable) {
        String keyword = studentNo == null ? "" : studentNo.trim();
        return attendanceRepository.findAdminPage(keyword, studentId, pageable);
    }

    public Optional<AttendanceInfo> findById(Long id) {
        return attendanceRepository.findById(id);
    }

    public List<AttendanceInfo> findByStudentId(Long studentId) {
        return attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId);
    }

    public Page<AttendanceInfo> findStudentPage(Long studentId, String courseName, Long courseId, Pageable pageable) {
        String keyword = courseName == null ? "" : courseName.trim();
        return attendanceRepository.findStudentPage(studentId, keyword, courseId, pageable);
    }

    public AttendanceInfo save(AttendanceInfo attendance) {
        return attendanceRepository.save(attendance);
    }

    public void deleteById(Long id) {
        attendanceRepository.deleteById(id);
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
            if (!attendanceRepository.existsById(id)) {
                throw new IllegalArgumentException("record not found: " + id);
            }
        }
        for (Long id : uniqueIds) {
            deleteById(id);
        }
        return uniqueIds.size();
    }

    public void fillRedundantFields(AttendanceInfo attendance) {
        if (attendance.getStudentId() != null) {
            StudentInfo student = studentInfoRepository.findById(attendance.getStudentId()).orElse(null);
            if (student != null) {
                attendance.setStudentNo(student.getStudentNo());
                attendance.setStudentName(student.getName());
                attendance.setClassId(student.getClassId());
                attendance.setClassName(student.getClassName());
            }
        }
        if (attendance.getCourseId() != null) {
            CourseInfo course = courseInfoRepository.findById(attendance.getCourseId()).orElse(null);
            if (course != null) {
                attendance.setCourseName(course.getCourseName());
            }
        }
    }
}