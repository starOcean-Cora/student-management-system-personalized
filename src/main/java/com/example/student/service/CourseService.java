package com.example.student.service;

import com.example.student.entity.CourseInfo;
import com.example.student.repository.CourseInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseInfoRepository courseInfoRepository;

    public CourseService(CourseInfoRepository courseInfoRepository) {
        this.courseInfoRepository = courseInfoRepository;
    }

    public List<CourseInfo> findAll() {
        return courseInfoRepository.findAll();
    }

    public Page<CourseInfo> findListPage(String courseName, String courseCode, Pageable pageable) {
        String keyword = courseName == null ? "" : courseName.trim();
        String selectedCode = courseCode == null ? "" : courseCode.trim();
        return courseInfoRepository.findListPage(keyword, selectedCode, pageable);
    }

    public List<String> findDistinctCourseCodes() {
        return courseInfoRepository.findDistinctCourseCodes();
    }

    public Optional<CourseInfo> findById(Long id) {
        return courseInfoRepository.findById(id);
    }

    public CourseInfo save(CourseInfo courseInfo) {
        return courseInfoRepository.save(courseInfo);
    }

    public void deleteById(Long id) {
        courseInfoRepository.deleteById(id);
    }

    public boolean isCourseCodeDuplicate(String courseCode, Long excludeId) {
        CourseInfo existing = courseInfoRepository.findByCourseCode(courseCode);
        if (existing == null) return false;
        return !existing.getId().equals(excludeId);
    }
}
