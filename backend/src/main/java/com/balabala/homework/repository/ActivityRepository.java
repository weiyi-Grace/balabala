package com.balabala.homework.repository;

import com.balabala.homework.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    /**
     * 查询最近的动态（按时间倒序）
     */
    List<Activity> findTop20ByOrderByCreatedAtDesc();
    
    /**
     * 查询指定班级的最近动态
     */
    List<Activity> findTop20ByClassIdOrderByCreatedAtDesc(Long classId);
    
    /**
     * 查询指定用户的最近动态
     */
    List<Activity> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 查询指定类型和班级的动态
     */
    List<Activity> findByTypeAndClassIdOrderByCreatedAtDesc(String type, Long classId);
}
