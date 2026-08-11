package com.balabala.homework.repository;

import com.balabala.homework.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    
    List<Answer> findBySubmissionId(Long submissionId);
    
    List<Answer> findByQuestionId(Long questionId);
    
    List<Answer> findByStatus(Integer status);  // 查找待AI复核的答案

    @Transactional
    void deleteBySubmissionId(Long submissionId);
}
