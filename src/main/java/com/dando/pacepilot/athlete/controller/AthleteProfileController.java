package com.dando.pacepilot.athlete.controller;

import com.dando.pacepilot.athlete.api.CreateAthleteProfileRequest;
import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.service.AthleteProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}