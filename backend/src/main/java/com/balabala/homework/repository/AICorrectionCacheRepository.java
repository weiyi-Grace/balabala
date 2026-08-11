package com.balabala.homework.repository;

import com.balabala.homework.entity.AICorrectionCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AICorrectionCacheRepository extends JpaRepository<AICorrectionCache, Long> {
    
    /**
     * 根据题目ID和答案哈希查找缓存
     */
    Optional<AICorrectionCache> findByQuestionIdAndAnswerHash(Long questionId, String answerHash);
}
