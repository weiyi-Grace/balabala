package com.balabala.homework.controller;

import com.alibaba.fastjson2.JSON;
import com.balabala.homework.dto.ErrorBookDTO;
import com.balabala.homework.dto.Result;
import com.balabala.homework.entity.ErrorBook;
import com.balabala.homework.entity.Question;
import com.balabala.homework.repository.ErrorBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/error-book")
@RequiredArgsConstructor
public class ErrorBookController {

    private final ErrorBookRepository errorBookRepository;

    /**
     * 转换为DTO
     */
    private ErrorBookDTO convertToDTO(ErrorBook error) {
        ErrorBookDTO dto = new ErrorBookDTO();
        dto.setId(error.getId());
        dto.setWrongAnswer(error.getWrongAnswer());
        dto.setCorrectAnswer(error.getCorrectAnswer());
        dto.setAnalysis(error.getAnalysis());
        dto.setKnowledgePoint(error.getKnowledgePoint());
        dto.setMasteryStatus(error.getMasteryStatus());
        dto.setNotes(error.getNotes());
        dto.setReviewCount(error.getReviewCount());
        dto.setLastReviewTime(error.getLastReviewTime());
        dto.setCreateTime(error.getCreateTime());
        
        // 设置题目相关信息
        Question question = error.getQuestion();
        if (question != null) {
            dto.setQuestionId(question.getId());
            dto.setQuestionContent(question.getContent());
            dto.setQuestionType(question.getType());
            dto.setSubject(question.getSubject());
            dto.setScore(question.getScore());
            dto.setDifficulty(question.getDifficulty());
            
            // 解析选项JSON或|分隔的字符串
            String optionsJson = question.getOptions();
            log.debug("题目ID: {}, 选项原始数据: {}", question.getId(), optionsJson);
            if (optionsJson != null && !optionsJson.isEmpty()) {
                try {
                    // 尝试JSON数组解析
                    if (optionsJson.trim().startsWith("[")) {
                        List<String> options = JSON.parseArray(optionsJson, String.class);
                        dto.setOptions(options);
                        log.debug("解析JSON选项成功: {}", options);
                    } else {
                        // |分隔的字符串格式
                        List<String> options = List.of(optionsJson.split("\\|"));
                        dto.setOptions(options);
                        log.debug("解析分隔符选项成功: {}", options);
                    }
                } catch (Exception e) {
                    log.error("解析选项失败, 题目ID: {}, optionsJson: {}", question.getId(), optionsJson, e);
                    dto.setOptions(null);
                }
            } else {
                log.warn("题目ID: {} 没有选项数据", question.getId());
            }
        } else {
            log.warn("错题ID: {} 没有关联题目", error.getId());
        }
        
        return dto;
    }

    /**
     * 获取学生错题本
     */
    @GetMapping("/student/{studentId}")
    public Result<List<ErrorBookDTO>> getStudentErrorBook(@PathVariable Long studentId) {
        List<ErrorBook> errors = errorBookRepository.findByStudentId(studentId);
        List<ErrorBookDTO> dtoList = errors.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return Result.success(dtoList);
    }

    /**
     * 根据知识点筛选
     */
    @GetMapping("/student/{studentId}/knowledge")
    public Result<List<ErrorBookDTO>> getByKnowledgePoint(
            @PathVariable Long studentId,
            @RequestParam String knowledgePoint) {
        List<ErrorBook> errors = errorBookRepository.findByStudentIdAndKnowledgePoint(studentId, knowledgePoint);
        List<ErrorBookDTO> dtoList = errors.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return Result.success(dtoList);
    }

    /**
     * 添加错题
     */
    @PostMapping
    public Result<ErrorBook> addError(@RequestBody ErrorBook errorBook) {
        ErrorBook saved = errorBookRepository.save(errorBook);
        return Result.success(saved);
    }

    /**
     * 更新掌握状态
     */
    @PutMapping("/{id}/mastery")
    public Result<Void> updateMasteryStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        ErrorBook error = errorBookRepository.findById(id).orElse(null);
        if (error != null) {
            error.setMasteryStatus(status);
            errorBookRepository.save(error);
        }
        return Result.success();
    }

    /**
     * 记录复习
     */
    @PostMapping("/{id}/review")
    public Result<Void> reviewError(@PathVariable Long id, @RequestParam String notes) {
        ErrorBook error = errorBookRepository.findById(id).orElse(null);
        if (error != null) {
            error.setReviewCount(error.getReviewCount() + 1);
            error.setNotes(notes);
            error.setLastReviewTime(java.time.LocalDateTime.now());
            errorBookRepository.save(error);
        }
        return Result.success();
    }

    /**
     * 删除错题
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteError(@PathVariable Long id) {
        errorBookRepository.deleteById(id);
        return Result.success();
    }
}
