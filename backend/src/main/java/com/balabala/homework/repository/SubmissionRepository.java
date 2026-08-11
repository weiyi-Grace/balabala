package com.balabala.homework.repository;

import com.balabala.homework.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    List<Submission> findByHomeworkId(Long homeworkId);
    
    List<Submission> findByStudentId(Long studentId);
    
    Optional<Submission> findByHomeworkIdAndStudentId(Long homeworkId, Long studentId);
    
    @Query("SELECT s FROM Submission s WHERE s.homework.id = :homeworkId AND s.status = :status")
    List<Submission> findByHomeworkAndStatus(@Param("homeworkId") Long homeworkId, @Param("status") Integer status);
    
    @Query("SELECT COUNT(s) FROM Submission s WHERE s.homework.id = :homeworkId AND s.status = 2")
    Long countCompletedByHomeworkId(@Param("homeworkId") Long homeworkId);
}
