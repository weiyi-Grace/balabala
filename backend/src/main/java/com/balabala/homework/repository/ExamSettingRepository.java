package com.balabala.homework.repository;

import com.balabala.homework.entity.ExamSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamSettingRepository extends JpaRepository<ExamSetting, Long> {
    
    Optional<ExamSetting> findByHomeworkId(Long homeworkId);
}
