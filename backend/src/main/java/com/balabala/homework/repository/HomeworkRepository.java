package com.balabala.homework.repository;

import com.balabala.homework.entity.Homework;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    
    List<Homework> findByTeacherId(Long teacherId);
    
    List<Homework> findByClassInfoId(Long classId);
    
    @Query("SELECT h FROM Homework h WHERE h.classInfo.id = :classId AND h.status = 1 ORDER BY h.deadline DESC")
    List<Homework> findActiveHomeworkByClassId(@Param("classId") Long classId);
    
    @Query("SELECT h FROM Homework h WHERE h.teacher.id = :teacherId AND " +
           "(:status IS NULL OR h.status = :status)")
    Page<Homework> findByTeacherAndStatus(@Param("teacherId") Long teacherId, 
                                           @Param("status") Integer status, 
                                           Pageable pageable);
}
