package com.dando.pacepilot.assistant.api;

import com.dando.pacepilot.assistant.TrainingAnswer;
import com.dando.pacepilot.assistant.TrainingAssistantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class TrainingAssistantController {

    private final TrainingAssistantService assistantService;

    public TrainingAssistantController(TrainingAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/ask")
    public TrainingAnswerResponse ask(@Valid @RequestBody AskTrainingAssistantRequest request) {
        TrainingAnswer answer = assistantService.ask(request.question());

        return TrainingAnswerResponse.from(answer);
    }
}