package com.dando.pacepilot.athlete.controller;

import com.dando.pacepilot.athlete.api.CreateAthleteProfileRequest;
import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.domain.DistanceUnit;
import com.dando.pacepilot.athlete.domain.ExperienceLevel;
import com.dando.pacepilot.athlete.domain.FitnessGoal;
import com.dando.pacepilot.athlete.domain.GoalType;
import com.dando.pacepilot.athlete.service.AthleteProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AthleteProfileController.class)
class AthleteProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AthleteProfileService profileService;

    @Test
    void createsProfile() throws Exception {
        UUID id = UUID.randomUUID();
        AthleteProfile profile = createProfile(id, "Dan");

        when(profileService.createProfile(any(CreateAthleteProfileRequest.class))).thenReturn(profile);

        mockMvc.perform(post("/api/athletes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRequestJson(4)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.displayName").value("Dan"))
                .andExpect(jsonPath("$.experienceLevel").value("BEGINNER"))
                .andExpect(jsonPath("$.goal.type").value("MARATHON"));

        verify(profileService).createProfile(any(CreateAthleteProfileRequest.class));
    }

    @Test
    void rejectsMoreThanSevenTrainingDays() throws Exception {
        mockMvc.perform(post("/api/athletes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRequestJson(8)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(profileService);
    }

    @Test
    void returnsAllProfiles() throws Exception {
        AthleteProfile firstProfile = createProfile(UUID.randomUUID(), "Dan");
        AthleteProfile secondProfile = createProfile(UUID.randomUUID(), "Taylor");

        when(profileService.findAllProfiles()).thenReturn(List.of(firstProfile, secondProfile));

        mockMvc.perform(get("/api/athletes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value("Dan"))
                .andExpect(jsonPath("$[1].displayName").value("Taylor"));

        verify(profileService).findAllProfiles();
    }

    @Test
    void returnsProfileById() throws Exception {
        UUID id = UUID.randomUUID();
        AthleteProfile profile = createProfile(id, "Dan");

        when(profileService.findProfileById(id)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/api/athletes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.displayName").value("Dan"));

        verify(profileService).findProfileById(id);
    }

    @Test
    void returnsNotFoundForUnknownProfile() throws Exception {
        UUID unknownId = UUID.randomUUID();

        when(profileService.findProfileById(unknownId)).thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/athletes/{id}", unknownId))
                .andExpect(status().isNotFound());

        verify(profileService).findProfileById(unknownId);
    }

    // Helper method to create sample athlete profile
    private AthleteProfile createProfile(UUID id, String displayName) {
        FitnessGoal goal = new FitnessGoal(
                GoalType.MARATHON,
                LocalDate.of(2099, 1, 1),
                240
        );

        return new AthleteProfile(
                id,
                displayName,
                ExperienceLevel.BEGINNER,
                4,
                15.0,
                DistanceUnit.MILES,
                goal
        );
    }

    // Helper method to create sample JSON request
    private String createRequestJson(int trainingDays) {
        return """
                {
                  "displayName": "Dan",
                  "experienceLevel": "BEGINNER",
                  "availableTrainingDaysPerWeek": %d,
                  "currentWeeklyRunningDistance": 15.0,
                  "runningDistanceUnit": "MILES",
                  "goal": {
                    "type": "MARATHON",
                    "targetDate": "2099-01-01",
                    "targetTimeMinutes": 240
                  }
                }
                """.formatted(trainingDays);
    }
}