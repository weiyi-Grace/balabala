package com.balabala.homework.repository;

import com.balabala.homework.entity.ClassTransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassTransferRequestRepository extends JpaRepository<ClassTransferRequest, Long> {
    
    List<ClassTransferRequest> findByStudentIdOrderByCreateTimeDesc(Long studentId);
    
    List<ClassTransferRequest> findByToClassTeacherIdAndStatusOrderByCreateTimeDesc(Long teacherId, Integer status);
    
    List<ClassTransferRequest> findByToClassIdAndStatusOrderByCreateTimeDesc(Long classId, Integer status);
    
    boolean existsByStudentIdAndToClassIdAndStatus(Long studentId, Long toClassId, Integer status);
}
