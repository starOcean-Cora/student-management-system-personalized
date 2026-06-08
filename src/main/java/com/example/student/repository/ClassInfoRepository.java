package com.example.student.repository;

import com.example.student.entity.ClassInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassInfoRepository extends JpaRepository<ClassInfo, Long> {
}