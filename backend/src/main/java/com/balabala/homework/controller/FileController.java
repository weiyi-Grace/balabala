package com.balabala.homework.controller;

import com.alibaba.fastjson2.JSONObject;
import com.balabala.homework.dto.Result;
import com.balabala.homework.service.DocumentParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final DocumentParserService documentParserService;

    /**
     * 上传并解析题目文件
     */
    @PostMapping("/upload/questions")
    public Result<List<JSONObject>> uploadQuestionFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        
        try {
            List<JSONObject> questions = documentParserService.parseQuestions(file);
            return Result.success(questions);
        } catch (IOException e) {
            return Result.error("文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析文本内容
     */
    @PostMapping("/parse/text")
    public Result<List<JSONObject>> parseText(@RequestBody String content) {
        List<JSONObject> questions = documentParserService.parseQuestionsFromText(content);
        return Result.success(questions);
    }
}
