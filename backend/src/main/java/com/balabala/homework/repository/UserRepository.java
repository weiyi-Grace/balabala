package com.balabala.homework.repository;

import com.balabala.homework.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    List<User> findByRole(Integer role);
    
    @Query("SELECT u FROM User u WHERE u.classInfo.id = :classId AND u.role = 3")
    List<User> findStudentsByClassId(@Param("classId") Long classId);
    
    // 查找未加入指定班级的学生（包括没有班级的学生）
    @Query("SELECT u FROM User u WHERE u.role = 3 AND (u.classInfo.id IS NULL OR u.classInfo.id != :classId)")
    List<User> findStudentsNotInClass(@Param("classId") Long classId);
}
