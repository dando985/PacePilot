package com.dando.pacepilot.trainingplan.api;

import com.dando.pacepilot.embedding.EmbeddingException;
import com.dando.pacepilot.generation.TextGenerationException;
import com.dando.pacepilot.trainingplan.domain.TrainingPlan;
import com.dando.pacepilot.trainingplan.exception.InvalidTrainingPlanException;
import com.dando.pacepilot.trainingplan.generation.TrainingPlanGenerationService;
import com.dando.pacepilot.trainingplan.service.TrainingPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class TrainingPlanController {

    private final TrainingPlanService planService;
    private final TrainingPlanGenerationService generationService;

    public TrainingPlanController(TrainingPlanService planService, TrainingPlanGenerationService generationService) {
        this.planService = planService;
        this.generationService = generationService;
    }

    @PostMapping("/api/athletes/{athleteId}/training-plans")
    public ResponseEntity<TrainingPlan> createPlan(@PathVariable UUID athleteId, @Valid @RequestBody CreateTrainingPlanRequest request) {
        TrainingPlan plan = planService.createPlan(athleteId, request);
        URI location = URI.create("/api/training-plans/" + plan.id());

        return ResponseEntity.created(location).body(plan);
    }

    @PostMapping("/api/athletes/{athleteId}/training-plans/generate")
    public ResponseEntity<TrainingPlan> generatePlan(@PathVariable UUID athleteId, @Valid @RequestBody GenerateTrainingPlanRequest request) {
        TrainingPlan plan = generationService.generatePlan(athleteId, request);
        URI location = URI.create("/api/training-plans/" + plan.id());

        return ResponseEntity.created(location).body(plan);
    }

    @GetMapping("/api/training-plans/{planId}")
    public TrainingPlan getPlanById(@PathVariable UUID planId) {
        return planService.getPlanById(planId);
    }

    @GetMapping("/api/athletes/{athleteId}/training-plans")
    public List<TrainingPlan> findPlansByAthleteId(@PathVariable UUID athleteId) {
        return planService.findPlansByAthleteId(athleteId);
    }

    @ExceptionHandler(InvalidTrainingPlanException.class)
    public ProblemDetail handleInvalidPlan(InvalidTrainingPlanException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({TextGenerationException.class, EmbeddingException.class})
    public ProblemDetail handleGenerationFailure(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }
}