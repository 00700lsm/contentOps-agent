package com.contentopsagent.question.controller;

import com.contentopsagent.question.dto.QuestionRequest;
import com.contentopsagent.question.dto.QuestionResponse;
import com.contentopsagent.question.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public QuestionResponse ask(@Valid @RequestBody QuestionRequest request) {
        return questionService.ask(request.question());
    }
}
