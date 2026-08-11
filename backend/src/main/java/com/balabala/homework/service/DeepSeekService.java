package com.balabala.homework.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.balabala.homework.entity.AICorrectionCache;
import com.balabala.homework.entity.Answer;
import com.balabala.homework.repository.AICorrectionCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekService {

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AICorrectionCacheRepository cacheRepository;

    /**
     * 调用DeepSeek API进行智能批改
     */
    public JSONObject correctAnswer(String question, String studentAnswer, String correctAnswer,
                                    String questionType, Integer fullScore, List<String> options) {
        try {
            String prompt = buildCorrectionPrompt(question, studentAnswer, correctAnswer, 
                                                 questionType, fullScore, options);
            
            String response = callDeepSeekAPI(prompt);
            return parseCorrectionResponse(response);
        } catch (Exception e) {
            log.error("AI批改失败", e);
            return buildFallbackResponse(studentAnswer, correctAnswer, fullScore);
        }
    }

    /**
     * 使用RAG检索知识库辅助批改
     */
    public JSONObject correctWithRAG(String question, String studentAnswer, String knowledgePoint) {
        // 检索相关知识
        String relevantKnowledge = retrieveKnowledge(knowledgePoint);
        
        String prompt = buildRAGPrompt(question, studentAnswer, relevantKnowledge);
        String response = callDeepSeekAPI(prompt);
        
        return parseCorrectionResponse(response);
    }

    /**
     * Agent智能批改助手 - 多轮推理
     */
    public JSONObject agentCorrect(String question, String studentAnswer, String correctAnswer,
                                   String questionType, Integer fullScore) {
        // Step 1: 分析题目类型和考察点
        String analysisPrompt = String.format("""
            作为教育AI助手，请分析这道%s题目的考察要点：
            题目：%s
            参考答案：%s
            
            请输出：
            1. 核心知识点
            2. 评分维度（准确性、完整性、逻辑性等）
            3. 各维度权重
            """, questionType, question, correctAnswer);
        
        String analysis = callDeepSeekAPI(analysisPrompt);

        // Step 2: 基于分析进行批改
        String correctionPrompt = String.format("""
            基于以下题目分析和学生答案，进行智能批改：
            
            【题目分析】
            %s
            
            【学生答案】
            %s
            
            【参考答案】
            %s
            
            【总分】%d分
            
            请按以下JSON格式输出批改结果：
            {
                "score": 得分（数字）,
                "confidence": 置信度（0-100）,
                "dimensions": {
                    "accuracy": 准确性评分（0-100）,
                    "completeness": 完整性评分（0-100）,
                    "logic": 逻辑性评分（0-100）
                },
                "analysis": "详细分析",
                "suggestions": "改进建议",
                "errors": ["错误点1", "错误点2"]
            }
            """, analysis, studentAnswer, correctAnswer, fullScore);

        String response = callDeepSeekAPI(correctionPrompt);
        return parseCorrectionResponse(response);
    }

    /**
     * 生成题目解析
     */
    public String generateAnalysis(String question, String correctAnswer) {
        String prompt = String.format("""
            请为以下题目生成详细解析：
            
            题目：%s
            答案：%s
            
            请包含：
            1. 解题思路
            2. 关键步骤
            3. 易错点提示
            4. 相关知识点拓展
            """, question, correctAnswer);
        
        return callDeepSeekAPI(prompt);
    }

    /**
     * AI智能答疑 - 专门用于错题答疑对话
     */
    public String chat(String question, String studentAnswer, String correctAnswer, 
                       String chatMessage, String knowledgePoint) {
        String knowledge = retrieveKnowledge(knowledgePoint);
        
        String prompt = String.format("""
            你是DeepSeek AI教育助手，正在帮助学生理解错题。
            
            【题目信息】
            题目：%s
            学生答案：%s
            正确答案：%s
            
            【相关知识背景】
            %s
            
            【学生的问题】
            %s
            
            请以友好、专业的口吻回答学生的问题，帮助他们理解这道错题。
            回答要求：
            1. 直接回应学生的具体问题
            2. 解释清楚错误原因
            3. 给出学习建议和解题技巧
            4. 语言通俗易懂，适当举例说明
            5. 如果是选择题，解释每个选项为什么对或错
            
            请直接输出回答内容，不需要JSON格式。
            """, question, studentAnswer, correctAnswer, knowledge, chatMessage);
        
        return callDeepSeekAPI(prompt);
    }

    /**
     * AI智能出题
     */
    public JSONObject generateQuestion(String subject, String knowledgePoint, 
                                       String difficulty, String questionType, String userPrompt) {
        String extraPrompt = userPrompt != null && !userPrompt.isEmpty() 
            ? "\n\n额外要求：" + userPrompt 
            : "";
        
        // 根据题型定义答案格式说明
        String answerFormat = switch (questionType) {
            case "single_choice" -> 
                "correctAnswer: 必须是单个字母 A/B/C/D 之一（绝对不能是文字描述）";
            case "multiple_choice" -> 
                "注意：这是多选题，必须有2-4个正确选项。correctAnswer: 多个正确选项的字母连续排列，如 AB、ACD、BC、ABCD 等。绝对禁止只返回单个字母（如A、B、C、D），多选题至少要有2个正确答案。";
            case "true_false" -> 
                "correctAnswer: 必须是\"正确\"或\"错误\"这两个汉字之一（绝对不能是字母或true/false）";
            case "fill_blank" -> 
                "correctAnswer: 中文文字答案，描述填空内容（绝对不能是字母A/B/C/D）";
            case "short_answer", "reading_comprehension" -> 
                "correctAnswer: 详细的中文文字描述作为参考答案（绝对不能是字母A/B/C/D，必须是一段话）";
            default -> 
                "correctAnswer: 正确答案";
        };
        
        String prompt = String.format("""
            请生成一道%s学科、%s难度、考察%s知识点的%s题目。%s
            
            要求：
            1. 题目内容要准确、清晰、符合该学科的教学要求
            2. 如果是选择题，必须提供4个选项
            3. 答案格式必须严格按照以下要求：%s
            
            请按以下JSON格式输出（不要添加 markdown 代码块标记）：
            {
                "content": "题目内容",
                "options": ["选项A", "选项B", "选项C", "选项D"],
                "correctAnswer": "%s",
                "analysis": "详细的答案解析",
                "score": 建议分值（数字）
            }
            """, subject, difficulty, knowledgePoint, questionType, extraPrompt, answerFormat, 
                questionType.equals("true_false") ? "正确" : "A");
        
        String response = callDeepSeekAPI(prompt);
        return parseJsonResponse(response);
    }

    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekAPI(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl, 
            HttpMethod.POST, 
            entity, 
            String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonResponse = JSON.parseObject(response.getBody());
            return jsonResponse.getJSONArray("choices")
                              .getJSONObject(0)
                              .getJSONObject("message")
                              .getString("content");
        }
        
        throw new RuntimeException("DeepSeek API调用失败");
    }

    /**
     * 检索相关知识（RAG）
     */
    private String retrieveKnowledge(String knowledgePoint) {
        // 这里可以从向量数据库检索相关知识
        // 简化实现，返回预设知识
        return switch (knowledgePoint) {
            case "修辞手法" -> """
                常见修辞手法：比喻、拟人、排比、夸张、对偶、设问、反问、借代等。
                判断方法：分析句子结构和表达效果。
                """;
            case "文言文翻译" -> """
                翻译原则：信（准确）、达（通顺）、雅（文雅）。
                注意：通假字、词类活用、特殊句式。
                """;
            default -> "相关知识点";
        };
    }

    /**
     * 构建批改Prompt
     */
    private String buildCorrectionPrompt(String question, String studentAnswer, 
                                        String correctAnswer, String questionType, 
                                        Integer fullScore, List<String> options) {
        StringBuilder optionsStr = new StringBuilder();
        if (options != null && !options.isEmpty()) {
            optionsStr.append("\n【选项】\n");
            char[] letters = {'A', 'B', 'C', 'D', 'E', 'F'};
            for (int i = 0; i < options.size() && i < letters.length; i++) {
                optionsStr.append(letters[i]).append(". ").append(options.get(i)).append("\n");
            }
        }
        
        // 根据题型定义不同的评判规则
        String scoringRules = switch (questionType) {
            case "multiple_choice" -> """
                【多选题严格评判规则】
                - 必须全部选对才给满分，少选、多选、错选均不得分
                - 只需判断"全对"或"不全对"，不需要给出具体分数
                - 如果不全对，分析说明漏选或错选了哪些选项
                """;
            case "single_choice", "true_false" -> """
                【单选题/判断题评判规则】
                - 答案正确给满分，答案错误给0分
                - 只需判断"对"或"错"
                """;
            case "fill_blank", "short_answer" -> """
                【填空题/简答题语义评判规则】
                - 允许语义相近的答案，只要核心意思正确即可
                - 使用语义相似度评判：如果学生答案与参考答案表达不同但意思相近，视为正确
                - 关注关键词是否出现，允许同义词替换
                - 如果意思基本正确，给满分；如果意思错误，给0分
                """;
            default -> """
                【通用评判规则】
                - 根据答案准确性评分
                """;
        };
        
        return String.format("""
            请作为资深教师，对以下%s题目进行批改：
            
            %s
            
            【题目】
            %s%s
            【参考答案】
            %s
            
            【学生答案】
            %s
            
            【总分】%d分
            
            【重要说明】
            请严格对比学生答案和参考答案：
            1. 如果学生答案与参考答案一致或意思相同 → isCorrect设为true，analysis说明正确之处
            2. 如果学生答案与参考答案不一致 → isCorrect设为false，analysis必须详细说明：
               - 学生答案错在哪里
               - 为什么参考答案是正确的
               - 相关知识点的解释
            
            请按以下JSON格式输出：
            {
                "score": 得分（整数，0或%d，只有全对才给满分）,
                "isCorrect": 是否正确（布尔值，true/false，必须严格判断）,
                "analysis": "详细点评：如果正确说明对在哪里；如果错误必须详细说明错在哪里并给出完整解析"
            }
            """, questionType, scoringRules, question, optionsStr.toString(), 
                 correctAnswer, studentAnswer, fullScore, fullScore);
    }

    /**
     * 构建RAG Prompt
     */
    private String buildRAGPrompt(String question, String studentAnswer, String knowledge) {
        return String.format("""
            基于以下知识背景，批改学生答案：
            
            【相关知识】
            %s
            
            【题目】
            %s
            
            【学生答案】
            %s
            
            请给出评分和详细分析。
            """, knowledge, question, studentAnswer);
    }

    /**
     * 解析题目生成响应
     */
    private JSONObject parseJsonResponse(String response) {
        try {
            // 提取JSON部分
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}") + 1;
            if (start >= 0 && end > start) {
                String jsonStr = response.substring(start, end);
                return JSON.parseObject(jsonStr);
            }
        } catch (Exception e) {
            log.error("解析题目响应失败", e);
        }
        return new JSONObject();
    }

    /**
     * 解析批改响应
     */
    private JSONObject parseCorrectionResponse(String response) {
        try {
            // 提取JSON部分
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}") + 1;
            if (start >= 0 && end > start) {
                String jsonStr = response.substring(start, end);
                return JSON.parseObject(jsonStr);
            }
        } catch (Exception e) {
            log.error("解析响应失败", e);
        }
        return new JSONObject();
    }

    /**
     * 构建备用响应
     */
    private JSONObject buildFallbackResponse(String studentAnswer, String correctAnswer, 
                                             Integer fullScore) {
        JSONObject result = new JSONObject();
        
        // 判断答案是否为空或未作答
        if (studentAnswer == null || studentAnswer.trim().isEmpty() || "未作答".equals(studentAnswer)) {
            result.put("score", 0);
            result.put("isCorrect", false);
            result.put("analysis", "学生未作答或答案为空。参考答案：" + correctAnswer + "。建议：请及时完成作业，认真审题作答。");
            return result;
        }
        
        result.put("score", fullScore / 2);
        result.put("isCorrect", false);
        result.put("analysis", "AI批改服务暂时不可用，已给出默认评分。学生答案：" + studentAnswer + "。参考答案：" + correctAnswer + "。请教师进行人工复核。");
        
        return result;
    }

    /**
     * 从缓存获取批改结果
     */
    public Optional<AICorrectionCache> getCorrectionFromCache(Long questionId, String studentAnswer) {
        String answerHash = generateAnswerHash(studentAnswer);
        return cacheRepository.findByQuestionIdAndAnswerHash(questionId, answerHash);
    }

    /**
     * 保存批改结果到缓存
     */
    public void saveToCache(Long questionId, String studentAnswer, String questionType, 
                           Integer score, String analysis, Boolean isCorrect) {
        try {
            String answerHash = generateAnswerHash(studentAnswer);
            
            // 检查是否已存在
            Optional<AICorrectionCache> existing = cacheRepository.findByQuestionIdAndAnswerHash(questionId, answerHash);
            if (existing.isPresent()) {
                // 更新使用次数
                AICorrectionCache cache = existing.get();
                cache.setUseCount(cache.getUseCount() + 1);
                cache.setAnalysis(analysis); // 更新解析
                cacheRepository.save(cache);
                log.info("AI批改缓存命中并更新，questionId={}, useCount={}", questionId, cache.getUseCount());
                return;
            }
            
            // 创建新缓存
            AICorrectionCache cache = new AICorrectionCache();
            cache.setQuestionId(questionId);
            cache.setAnswerHash(answerHash);
            cache.setQuestionType(questionType);
            cache.setScore(score);
            cache.setAnalysis(analysis);
            cache.setIsCorrect(isCorrect);
            cache.setStudentAnswer(studentAnswer.length() > 1000 ? studentAnswer.substring(0, 1000) : studentAnswer);
            cache.setUseCount(1);
            
            cacheRepository.save(cache);
            log.info("AI批改结果已缓存，questionId={}", questionId);
        } catch (Exception e) {
            log.error("保存AI批改缓存失败", e);
        }
    }

    /**
     * 生成答案的MD5哈希值
     */
    private String generateAnswerHash(String answer) {
        if (answer == null) {
            return "null";
        }
        // 标准化答案：去除空白字符，转为小写
        String normalized = answer.trim().toLowerCase().replaceAll("\\s+", "");
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // 如果MD5不可用，使用简单哈希
            return String.valueOf(normalized.hashCode());
        }
    }

    /**
     * 使用缓存进行批改（带缓存机制）
     */
    public JSONObject correctAnswerWithCache(Long questionId, String question, String studentAnswer, 
                                              String correctAnswer, String questionType, 
                                              Integer fullScore, List<String> options) {
        // 1. 先查缓存
        Optional<AICorrectionCache> cache = getCorrectionFromCache(questionId, studentAnswer);
        if (cache.isPresent()) {
            AICorrectionCache cached = cache.get();
            log.info("AI批改缓存命中，questionId={}, useCount={}", questionId, cached.getUseCount());
            
            JSONObject result = new JSONObject();
            result.put("score", cached.getScore());
            result.put("isCorrect", cached.getIsCorrect());
            result.put("analysis", cached.getAnalysis());
            result.put("fromCache", true);
            return result;
        }
        
        // 2. 缓存未命中，调用AI
        log.info("AI批改缓存未命中，调用DeepSeek API，questionId={}", questionId);
        JSONObject result = correctAnswer(question, studentAnswer, correctAnswer, questionType, fullScore, options);
        
        // 3. 保存到缓存（确保有解析内容）
        Integer score = result.getInteger("score");
        Boolean isCorrect = result.getBoolean("isCorrect");
        String analysis = result.getString("analysis");
        
        // 如果解析为空，生成默认解析
        if (analysis == null || analysis.trim().isEmpty()) {
            if (Boolean.TRUE.equals(isCorrect)) {
                analysis = "回答正确！" + studentAnswer + " 是正确答案。";
            } else {
                analysis = "回答错误。学生答案：" + studentAnswer + "。参考答案：" + correctAnswer + "。请核对答案并复习相关知识点。";
            }
            result.put("analysis", analysis);
        }
        
        saveToCache(questionId, studentAnswer, questionType, score, analysis, isCorrect);
        
        return result;
    }
}
