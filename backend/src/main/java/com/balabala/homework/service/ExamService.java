package com.balabala.homework.service;

import com.balabala.homework.dto.CreateExamRequest;
import com.balabala.homework.entity.*;
import com.balabala.homework.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final HomeworkRepository homeworkRepository;
    private final ExamSettingRepository examSettingRepository;
    private final HomeworkQuestionRepository homeworkQuestionRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ClassRepository classRepository;

    /**
     * 创建考试
     */
    @Transactional
    public Homework createExam(CreateExamRequest request) {
        // 创建考试基本信息
        Homework exam = new Homework();
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setType("exam");
        exam.setSubject(request.getSubject());
        exam.setStartTime(request.getStartTime());
        exam.setDeadline(request.getDeadline());
        exam.setDuration(request.getDuration());
        exam.setStatus(1); // 已发布
        exam.setAllowResubmit(false); // 考试不允许重复提交
        
        // 设置教师
        User teacher = userRepository.findById(request.getTeacherId()).orElse(null);
        exam.setTeacher(teacher);
        
        // 设置班级
        ClassInfo classInfo = classRepository.findById(request.getClassId()).orElse(null);
        exam.setClassInfo(classInfo);
        
        Homework savedExam = homeworkRepository.save(exam);
        
        // 创建考试题目关联
        if (request.getQuestionIds() != null) {
            int order = 1;
            for (Long questionId : request.getQuestionIds()) {
                Question question = questionRepository.findById(questionId).orElse(null);
                if (question != null) {
                    HomeworkQuestion hq = new HomeworkQuestion();
                    hq.setHomework(savedExam);
                    hq.setQuestion(question);
                    hq.setQuestionOrder(order++);
                    homeworkQuestionRepository.save(hq);
                }
            }
        }
        
        // 创建考试防作弊设置
        ExamSetting setting = new ExamSetting();
        setting.setHomework(savedExam);
        setting.setShuffleQuestions(request.getShuffleQuestions());
        setting.setShuffleOptions(request.getShuffleOptions());
        setting.setFullScreenMode(request.getFullScreenMode());
        setting.setPreventCopyPaste(request.getPreventCopyPaste());
        setting.setLimitSwitchWindow(request.getLimitSwitchWindow());
        setting.setMaxSwitchTimes(request.getMaxSwitchTimes());
        examSettingRepository.save(setting);
        
        return savedExam;
    }

    /**
     * 获取考试设置
     */
    public ExamSetting getExamSetting(Long examId) {
        return examSettingRepository.findByHomeworkId(examId).orElse(null);
    }

    /**
     * 检查考试是否正在进行
     */
    public boolean isExamActive(Long examId) {
        Homework exam = homeworkRepository.findById(examId).orElse(null);
        if (exam == null || !"exam".equals(exam.getType())) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(exam.getStartTime()) && now.isBefore(exam.getDeadline());
    }

    /**
     * 检查学生是否还能进入考试
     */
    public boolean canEnterExam(Long examId, Long studentId) {
        if (!isExamActive(examId)) {
            return false;
        }
        
        // 检查是否已经交卷
        // 这里可以添加更多逻辑，比如检查提交记录
        return true;
    }
}
