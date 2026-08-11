package com.balabala.homework.repository;

import com.balabala.homework.entity.ClassInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<ClassInfo, Long> {
    
    Optional<ClassInfo> findByInviteCode(String inviteCode);
    
    List<ClassInfo> findByTeacherId(Long teacherId);
    
    boolean existsByInviteCode(String inviteCode);
}
