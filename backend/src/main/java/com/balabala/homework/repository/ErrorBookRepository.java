package com.balabala.homework.repository;

import com.balabala.homework.entity.ErrorBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorBookRepository extends JpaRepository<ErrorBook, Long> {
    
    @Query("SELECT e FROM ErrorBook e JOIN FETCH e.question JOIN FETCH e.student WHERE e.student.id = :studentId")
    List<ErrorBook> findByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT e FROM ErrorBook e JOIN FETCH e.question JOIN FETCH e.student WHERE e.student.id = :studentId AND e.knowledgePoint = :knowledgePoint")
    List<ErrorBook> findByStudentIdAndKnowledgePoint(@Param("studentId") Long studentId, @Param("knowledgePoint") String knowledgePoint);
    
    List<ErrorBook> findByStudentIdAndMasteryStatus(Long studentId, Integer masteryStatus);
    
    // 根据学生ID和题目ID查询，用于检查错题是否已存在
    List<ErrorBook> findByStudentIdAndQuestionId(Long studentId, Long questionId);
    
    // 根据班级ID查询错题（通过学生关联）
    @Query("SELECT e FROM ErrorBook e JOIN FETCH e.question JOIN FETCH e.student WHERE e.student.classInfo.id = :classId")
    List<ErrorBook> findByClassId(@Param("classId") Long classId);
}
