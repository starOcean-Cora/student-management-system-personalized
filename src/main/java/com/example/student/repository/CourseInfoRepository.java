package com.example.student.repository;

import com.example.student.entity.CourseInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseInfoRepository extends JpaRepository<CourseInfo, Long> {
    boolean existsByCourseCode(String courseCode);
    CourseInfo findByCourseCode(String courseCode);

    @Query("select c from CourseInfo c " +
            "where (:courseName is null or :courseName = '' or c.courseName like concat('%', :courseName, '%')) " +
            "and (:courseCode is null or :courseCode = '' or c.courseCode = :courseCode)")
    Page<CourseInfo> findListPage(@Param("courseName") String courseName,
                                  @Param("courseCode") String courseCode,
                                  Pageable pageable);

    @Query("select distinct c.courseCode from CourseInfo c where c.courseCode is not null and c.courseCode <> '' order by c.courseCode")
    List<String> findDistinctCourseCodes();
}
