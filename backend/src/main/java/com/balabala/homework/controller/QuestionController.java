package com.balabala.homework.controller;

import com.balabala.homework.dto.Result;
import com.balabala.homework.entity.Question;
import com.balabala.homework.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionRepository questionRepository;

    /**
     * 获取题目列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<Question>> getQuestionList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Question> questions = questionRepository.findByConditions(keyword, subject, type, difficulty, pageable);
        return Result.success(questions);
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/{id}")
    public Result<Question> getQuestionById(@PathVariable Long id) {
        Question question = questionRepository.findById(id).orElse(null);
        return Result.success(question);
    }

    /**
     * 创建题目
     */
    @PostMapping
    public Result<Question> createQuestion(@RequestBody Question question) {
        Question saved = questionRepository.save(question);
        return Result.success(saved);
    }

    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    public Result<Question> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        Question updated = questionRepository.save(question);
        return Result.success(updated);
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        Question question = questionRepository.findById(id).orElse(null);
        if (question != null) {
            question.setStatus(0);
            questionRepository.save(question);
        }
        return Result.success();
    }

    /**
     * 根据知识点查询题目
     */
    @GetMapping("/knowledge/{knowledgePoint}")
    public Result<List<Question>> getQuestionsByKnowledgePoint(@PathVariable String knowledgePoint) {
        List<Question> questions = questionRepository.findByKnowledgePoint(knowledgePoint);
        return Result.success(questions);
    }

    /**
     * 批量导入题目
     */
    @PostMapping("/batch")
    public Result<List<Question>> batchImport(@RequestBody List<Question> questions) {
        List<Question> saved = questionRepository.saveAll(questions);
        return Result.success(saved);
    }
}
