package com.balabala.homework.controller;

import com.alibaba.fastjson2.JSONObject;
import com.balabala.homework.dto.AIChatRequest;
import com.balabala.homework.dto.AICorrectRequest;
import com.balabala.homework.dto.GenerateAnalysisRequest;
import com.balabala.homework.dto.GenerateQuestionRequest;
import com.balabala.homework.dto.Result;
import com.balabala.homework.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {

    private final DeepSeekService deepSeekService;

    /**
     * AI智能批改 - 基础批改
     */
    @PostMapping("/correct")
    public Result<JSONObject> correctAnswer(@RequestBody AICorrectRequest request) {
        JSONObject result = deepSeekService.correctAnswer(
            request.getQuestion(),
            request.getStudentAnswer(),
            request.getCorrectAnswer(),
            request.getQuestionType(),
            request.getFullScore(),
            request.getOptions()  // 添加options参数
        );
        return Result.success(result);
    }

    /**
     * AI智能批改 - 使用RAG知识库
     */
    @PostMapping("/correct/rag")
    public Result<JSONObject> correctWithRAG(@RequestBody AICorrectRequest request) {
        JSONObject result = deepSeekService.correctWithRAG(
            request.getQuestion(),
            request.getStudentAnswer(),
            request.getKnowledgePoint()
        );
        return Result.success(result);
    }

    /**
     * AI智能批改 - Agent多轮推理
     */
    @PostMapping("/correct/agent")
    public Result<JSONObject> agentCorrect(@RequestBody AICorrectRequest request) {
        JSONObject result = deepSeekService.agentCorrect(
            request.getQuestion(),
            request.getStudentAnswer(),
            request.getCorrectAnswer(),
            request.getQuestionType(),
            request.getFullScore()
        );
        return Result.success(result);
    }

    /**
     * 生成题目解析
     */
    @PostMapping("/analysis")
    public Result<String> generateAnalysis(@RequestBody GenerateAnalysisRequest request) {
        String analysis = deepSeekService.generateAnalysis(
            request.getQuestion(),
            request.getCorrectAnswer()
        );
        return Result.success(analysis);
    }

    /**
     * AI智能答疑 - 错题答疑对话
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody AIChatRequest request) {
        String response = deepSeekService.chat(
            request.getQuestion(),
            request.getStudentAnswer(),
            request.getCorrectAnswer(),
            request.getChatMessage(),
            request.getKnowledgePoint()
        );
        return Result.success(response);
    }

    /**
     * AI智能出题
     */
    @PostMapping("/generate-question")
    public Result<JSONObject> generateQuestion(@RequestBody GenerateQuestionRequest request) {
        JSONObject question = deepSeekService.generateQuestion(
            request.getSubject(),
            request.getKnowledgePoint(),
            request.getDifficulty(),
            request.getQuestionType(),
            request.getPrompt()
        );
        return Result.success(question);
    }
}
