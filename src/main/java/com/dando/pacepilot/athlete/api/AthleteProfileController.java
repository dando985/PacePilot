package com.dando.pacepilot.athlete.api;

import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.exception.AthleteProfileNotFoundException;
import com.dando.pacepilot.athlete.service.AthleteProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/athletes")
public class AthleteProfileController {

    private final AthleteProfileService profileService;

    public AthleteProfileController(AthleteProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AthleteProfile createProfile(@Valid @RequestBody CreateAthleteProfileRequest request) {
        return profileService.createProfile(request);
    }

    @PutMapping("/{id}")
    public AthleteProfile updateProfile(@PathVariable UUID id, @Valid @RequestBody UpdateAthleteProfileRequest request) {
        return profileService.updateProfile(id, request);
    }

    @GetMapping("/me")
    public AthleteProfile getCurrentProfile() {
        return profileService.getCurrentProfile();
    }

    @GetMapping("/{id}")
    public AthleteProfile getProfileById(@PathVariable UUID id) {
        return profileService.getProfileById(id);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleProfileFailure(ResponseStatusException exception) {
        String detail = exception.getReason() == null ? "Unable to complete the athlete profile request." : exception.getReason();

        return ProblemDetail.forStatusAndDetail(exception.getStatusCode(), detail);
    }

    @ExceptionHandler(AthleteProfileNotFoundException.class)
    public ProblemDetail handleProfileNotFound(AthleteProfileNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalidRequest(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(message -> message != null && !message.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.joining(" "));

        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail.isBlank() ? "Invalid athlete profile details." : detail);
    }
}