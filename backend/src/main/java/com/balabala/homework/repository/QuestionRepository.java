package com.balabala.homework.repository;

import com.balabala.homework.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    @Query("SELECT q FROM Question q WHERE q.status = 1 AND " +
           "(:keyword IS NULL OR q.content LIKE %:keyword%) AND " +
           "(:subject IS NULL OR q.subject = :subject) AND " +
           "(:type IS NULL OR q.type = :type) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty)")
    Page<Question> findByConditions(@Param("keyword") String keyword,
                                    @Param("subject") String subject,
                                    @Param("type") String type,
                                    @Param("difficulty") String difficulty,
                                    Pageable pageable);
    
    List<Question> findByCreatorId(Long creatorId);
    
    @Query("SELECT q FROM Question q WHERE q.status = 1 AND q.knowledgePoint = :knowledgePoint")
    List<Question> findByKnowledgePoint(@Param("knowledgePoint") String knowledgePoint);
}
