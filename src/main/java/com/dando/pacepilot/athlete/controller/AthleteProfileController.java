package com.dando.pacepilot.athlete.controller;

import com.dando.pacepilot.athlete.api.CreateAthleteProfileRequest;
import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.service.AthleteProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public List<AthleteProfile> getAllProfiles() {
        return profileService.findAllProfiles();
    }

    @GetMapping("/{id}")
    public AthleteProfile getProfileById(@PathVariable UUID id) {
        return profileService
                .findProfileById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Athlete profile was not found"
                        )
                );
    }
}