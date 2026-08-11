package com.balabala.homework.dto;

import lombok.Data;
import java.util.List;

@Data
public class SubmitHomeworkRequest {
    private Long homeworkId;
    private Long studentId;
    private List<AnswerDTO> answers;

    @Data
    public static class AnswerDTO {
        private Long questionId;
        private String content;
    }
}
