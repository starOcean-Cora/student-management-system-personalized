package com.example.student.service;

import com.example.student.entity.ClassInfo;
import com.example.student.repository.ClassInfoRepository;
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