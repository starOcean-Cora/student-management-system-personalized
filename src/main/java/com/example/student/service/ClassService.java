package com.example.student.service;

import com.example.student.entity.ClassInfo;
import com.example.student.repository.ClassInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClassService {

    private final ClassInfoRepository classInfoRepository;

    public ClassService(ClassInfoRepository classInfoRepository) {
        this.classInfoRepository = classInfoRepository;
    }

    public List<ClassInfo> findAll() {
        return classInfoRepository.findAll();
    }

    public Page<ClassInfo> findAdminPage(String className, String major, Pageable pageable) {
        String keyword = className == null ? "" : className.trim();
        String selectedMajor = major == null ? "" : major.trim();
        return classInfoRepository.findAdminPage(keyword, selectedMajor, pageable);
    }

    public List<String> findDistinctMajors() {
        return classInfoRepository.findDistinctMajors();
    }

    public Optional<ClassInfo> findById(Long id) {
        return classInfoRepository.findById(id);
    }

    public ClassInfo save(ClassInfo classInfo) {
        return classInfoRepository.save(classInfo);
    }

    public void deleteById(Long id) {
        classInfoRepository.deleteById(id);
    }
}
