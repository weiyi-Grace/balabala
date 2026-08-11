package com.balabala.homework.repository;

import com.balabala.homework.entity.HomeworkQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkQuestionRepository extends JpaRepository<HomeworkQuestion, Long> {
    
    List<HomeworkQuestion> findByHomeworkId(Long homeworkId);
    
    void deleteByHomeworkId(Long homeworkId);
}
