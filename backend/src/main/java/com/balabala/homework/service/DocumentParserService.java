package com.balabala.homework.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DocumentParserService {

    /**
     * 解析Word/TXT文档中的题目
     * 支持格式：
     * 1.【选择】(5分) 题目内容 A.选项 B.选项 答案: B
     * 2.【填空】(3分) 题目内容________ 答案: 答案
     * 3.【判断】(2分) 题目内容 答案: 正确/错误
     * 4.【阅读】(15分) 题目内容 答案: 参考答案
     */
    public List<JSONObject> parseQuestions(MultipartFile file) throws IOException {
        String content = readFileContent(file);
        return parseQuestionsFromText(content);
    }

    /**
     * 从文本解析题目
     */
    public List<JSONObject> parseQuestionsFromText(String text) {
        List<JSONObject> questions = new ArrayList<>();
        
        // 按题号分割题目
        Pattern pattern = Pattern.compile("(\\d+)\\.【(.+?)】\\((\\d+)分\\)(.+?)(?=\\d+\\.【|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            try {
                JSONObject question = new JSONObject();
                question.put("order", Integer.parseInt(matcher.group(1)));
                question.put("type", parseType(matcher.group(2)));
                question.put("score", Integer.parseInt(matcher.group(3)));
                
                String content = matcher.group(4).trim();
                parseQuestionContent(question, content);
                
                questions.add(question);
            } catch (Exception e) {
                log.error("解析题目失败: {}", matcher.group(), e);
            }
        }
        
        return questions;
    }

    /**
     * 解析题目类型
     */
    private String parseType(String typeStr) {
        return switch (typeStr) {
            case "选择" -> "single_choice";
            case "多选" -> "multiple_choice";
            case "填空" -> "fill_blank";
            case "判断" -> "true_false";
            case "阅读" -> "reading_comprehension";
            case "简答" -> "short_answer";
            default -> "short_answer";
        };
    }

    /**
     * 解析题目内容
     */
    private void parseQuestionContent(JSONObject question, String content) {
        // 提取答案
        Pattern answerPattern = Pattern.compile("答案[:：]\\s*(.+?)(?=\\n|$)");
        Matcher answerMatcher = answerPattern.matcher(content);
        
        String answer = "";
        if (answerMatcher.find()) {
            answer = answerMatcher.group(1).trim();
            question.put("correctAnswer", answer);
            // 移除答案部分
            content = content.substring(0, answerMatcher.start()).trim();
        }

        String type = question.getString("type");
        
        switch (type) {
            case "single_choice", "multiple_choice" -> parseChoiceQuestion(question, content, answer);
            case "fill_blank" -> {
                question.put("content", content.replaceAll("_{3,}", "________"));
            }
            case "true_false" -> {
                question.put("content", content);
                question.put("correctAnswer", answer.contains("正确") || answer.contains("对"));
            }
            case "reading_comprehension" -> {
                // 解析阅读题：区分文章和题目
                parseReadingQuestion(question, content, answer);
            }
            default -> question.put("content", content);
        }
    }

    /**
     * 解析选择题
     */
    private void parseChoiceQuestion(JSONObject question, String content, String answer) {
        // 提取选项 A. B. C. D.
        Pattern optionPattern = Pattern.compile("([A-F])\\.\\s*(.+?)(?=[A-F]\\.|$)", Pattern.DOTALL);
        Matcher optionMatcher = optionPattern.matcher(content);
        
        JSONArray options = new JSONArray();
        int optionIndex = 0;
        int lastEnd = 0;
        
        while (optionMatcher.find()) {
            if (lastEnd == 0) {
                // 第一个选项前的内容是题目
                String questionContent = content.substring(0, optionMatcher.start()).trim();
                question.put("content", questionContent);
            }
            
            options.add(optionMatcher.group(2).trim().replaceAll("\\s+", " "));
            lastEnd = optionMatcher.end();
            optionIndex++;
        }
        
        // 如果没有匹配到选项格式，尝试按行分割
        if (options.isEmpty()) {
            String[] lines = content.split("\\n");
            StringBuilder questionContent = new StringBuilder();
            
            for (String line : lines) {
                line = line.trim();
                if (line.matches("^[A-F][\\.\\u3002]\\s*.+")) {
                    options.add(line.substring(2).trim());
                } else {
                    questionContent.append(line).append("\n");
                }
            }
            
            question.put("content", questionContent.toString().trim());
        }
        
        question.put("options", options);
        
        // 解析答案索引
        if (!answer.isEmpty()) {
            String[] answerLetters = answer.split("[;；,，]");
            JSONArray correctAnswers = new JSONArray();
            
            for (String ans : answerLetters) {
                char letter = ans.trim().toUpperCase().charAt(0);
                correctAnswers.add(letter - 'A');  // 转换为0-based索引
            }
            
            question.put("correctAnswer", correctAnswers.size() == 1 ? correctAnswers.get(0) : correctAnswers);
        }
    }

    /**
     * 解析阅读理解题
     */
    private void parseReadingQuestion(JSONObject question, String content, String answer) {
        // 尝试区分文章和题目
        Pattern passagePattern = Pattern.compile("(.+?)问题[:：](.+)", Pattern.DOTALL);
        Matcher passageMatcher = passagePattern.matcher(content);
        
        if (passageMatcher.find()) {
            question.put("passage", passageMatcher.group(1).trim());
            question.put("content", passageMatcher.group(2).trim());
        } else {
            question.put("content", content);
        }
        
        question.put("correctAnswer", answer);
    }

    /**
     * 读取文件内容
     */
    private String readFileContent(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
}
