package com.example.student.repository;

import com.example.student.entity.ClassInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassInfoRepository extends JpaRepository<ClassInfo, Long> {

    @Query("select c from ClassInfo c " +
            "where (:className is null or :className = '' or c.className like concat('%', :className, '%')) " +
            "and (:major is null or :major = '' or c.major = :major)")
    Page<ClassInfo> findAdminPage(@Param("className") String className,
                                  @Param("major") String major,
                                  Pageable pageable);

    @Query("select distinct c.major from ClassInfo c where c.major is not null and c.major <> '' order by c.major")
    List<String> findDistinctMajors();
}
