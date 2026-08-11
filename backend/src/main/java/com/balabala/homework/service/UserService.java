package com.balabala.homework.service;

import com.balabala.homework.dto.UserStatsDTO;
import com.balabala.homework.entity.User;
import com.balabala.homework.repository.ErrorBookRepository;
import com.balabala.homework.repository.HomeworkRepository;
import com.balabala.homework.repository.SubmissionRepository;
import com.balabala.homework.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final HomeworkRepository homeworkRepository;
    private final ErrorBookRepository errorBookRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getUsersByRole(Integer role) {
        return userRepository.findByRole(role);
    }

    public List<User> getStudentsByClassId(Long classId) {
        return userRepository.findStudentsByClassId(classId);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 获取用户统计信息
     */
    public UserStatsDTO getUserStats(Long userId) {
        UserStatsDTO stats = new UserStatsDTO();
        
        // 获取学生的所有提交
        var submissions = submissionRepository.findByStudentId(userId);
        
        // 已完成作业数
        long completedCount = submissions.stream()
                .filter(s -> s.getStatus() == 2)
                .count();
        stats.setHomeworkCount((int) completedCount);
        
        // 平均分
        double avgScore = submissions.stream()
                .filter(s -> s.getTotalScore() != null && s.getStatus() == 2)
                .mapToInt(s -> s.getTotalScore())
                .average()
                .orElse(0.0);
        stats.setAvgScore(Math.round(avgScore * 10) / 10.0);
        
        // 总作业数
        User user = getUserById(userId);
        if (user != null && user.getClassInfo() != null) {
            int totalHomework = homeworkRepository.findByClassInfoId(user.getClassInfo().getId()).size();
            stats.setTotalHomework(totalHomework);
            stats.setCompletionRate(totalHomework > 0 ? (completedCount * 100.0 / totalHomework) : 0);
        } else {
            stats.setTotalHomework(0);
            stats.setCompletionRate(0.0);
        }
        
        // 错题统计
        var errors = errorBookRepository.findByStudentId(userId);
        stats.setErrorCount(errors.size());
        
        long masteredCount = errors.stream()
                .filter(e -> e.getMasteryStatus() == 1)
                .count();
        stats.setMasteredErrorCount((int) masteredCount);
        
        return stats;
    }

    /**
     * 搜索未加入指定班级的学生
     */
    public List<User> searchStudentsNotInClass(String keyword, Long excludeClassId) {
        // 获取未在该班级的所有学生
        List<User> students = userRepository.findStudentsNotInClass(excludeClassId);
        
        // 根据关键词过滤（姓名或学号包含关键词）
        if (keyword != null && !keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            return students.stream()
                    .filter(s -> {
                        String realName = s.getRealName() != null ? s.getRealName().toLowerCase() : "";
                        String username = s.getUsername() != null ? s.getUsername().toLowerCase() : "";
                        return realName.contains(lowerKeyword) || username.contains(lowerKeyword);
                    })
                    .limit(20) // 限制返回数量
                    .toList();
        }
        
        return students.stream().limit(20).toList();
    }
}
