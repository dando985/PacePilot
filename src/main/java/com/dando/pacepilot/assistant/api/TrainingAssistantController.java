package com.dando.pacepilot.assistant.api;

import com.dando.pacepilot.assistant.TrainingAnswer;
import com.dando.pacepilot.assistant.TrainingAssistantService;
import com.dando.pacepilot.embedding.EmbeddingException;
import com.dando.pacepilot.generation.TextGenerationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
    public TrainingAnswer ask(@Valid @RequestBody AskTrainingAssistantRequest request) {
        return assistantService.ask(request.athleteProfileId(), request.question(), request.trainingPlanId());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidRequest(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({TextGenerationException.class, EmbeddingException.class})
    public ProblemDetail handleGenerationFailure(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }
}