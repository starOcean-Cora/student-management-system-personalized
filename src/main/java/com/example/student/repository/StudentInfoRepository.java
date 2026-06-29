package com.example.student.repository;

import com.example.student.entity.StudentInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentInfoRepository extends JpaRepository<StudentInfo, Long> {
    boolean existsByStudentNo(String studentNo);
    StudentInfo findByStudentNo(String studentNo);

    @Query("select s from StudentInfo s " +
            "where (:name is null or :name = '' or s.name like concat('%', :name, '%')) " +
            "and (:classId is null or s.classId = :classId)")
    Page<StudentInfo> findAdminPage(@Param("name") String name,
                                    @Param("classId") Long classId,
                                    Pageable pageable);
}
