package com.dando.pacepilot.athlete.service;

import com.dando.pacepilot.athlete.api.CreateAthleteProfileRequest;
import com.dando.pacepilot.athlete.api.CreateFitnessGoalRequest;
import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.shared.domain.DistanceUnit;
import com.dando.pacepilot.athlete.domain.ExperienceLevel;
import com.dando.pacepilot.athlete.domain.FitnessGoal;
import com.dando.pacepilot.athlete.domain.GoalType;
import com.dando.pacepilot.athlete.exception.AthleteProfileNotFoundException;
import com.dando.pacepilot.athlete.repository.AthleteProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AthleteProfileServiceTest {

    @Mock
    private AthleteProfileRepository profileRepository;

    @InjectMocks
    private AthleteProfileService profileService;

    @Test
    void createsAndSavesProfile() {
        CreateFitnessGoalRequest goalRequest =
                new CreateFitnessGoalRequest(
                        GoalType.MARATHON,
                        LocalDate.of(2099, 1, 1),
                        240
                );

        CreateAthleteProfileRequest request =
                new CreateAthleteProfileRequest(
                        "Dan",
                        ExperienceLevel.BEGINNER,
                        4,
                        15.0,
                        DistanceUnit.MILES,
                        goalRequest
                );

        // Whenever service saves an athlete, mock repository should return the same athlete object it received
        when(profileRepository.save(any(AthleteProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AthleteProfile result = profileService.createProfile(request);

        assertThat(result.id()).isNotNull();
        assertThat(result.displayName()).isEqualTo("Dan");
        assertThat(result.experienceLevel()).isEqualTo(ExperienceLevel.BEGINNER);
        assertThat(result.availableTrainingDaysPerWeek()).isEqualTo(4);
        assertThat(result.currentWeeklyRunningDistance()).isEqualTo(15.0);
        assertThat(result.runningDistanceUnit()).isEqualTo(DistanceUnit.MILES);
        assertThat(result.goal().type()).isEqualTo(GoalType.MARATHON);
        assertThat(result.goal().targetTimeMinutes()).isEqualTo(240);

        verify(profileRepository).save(result);
    }

    @Test
    void getsProfileById() {
        AthleteProfile expectedProfile = createProfile(UUID.randomUUID(), "Dan");

        when(profileRepository.findById(expectedProfile.id())).thenReturn(Optional.of(expectedProfile));

        AthleteProfile result = profileService.getProfileById(expectedProfile.id());

        assertThat(result).isSameAs(expectedProfile);

        verify(profileRepository).findById(expectedProfile.id());
    }

    @Test
    void returnsAllProfiles() {
        AthleteProfile firstProfile = createProfile(UUID.randomUUID(), "Dan");
        AthleteProfile secondProfile = createProfile(UUID.randomUUID(), "Taylor");

        when(profileRepository.findAll()).thenReturn(List.of(firstProfile, secondProfile));

        assertThat(profileService.findAllProfiles()).containsExactly(firstProfile, secondProfile);

        verify(profileRepository).findAll();
    }

    @Test
    void throwsExceptionForUnknownProfile() {
        UUID unknownId = UUID.randomUUID();

        when(profileRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfileById(unknownId))
                .isInstanceOf(AthleteProfileNotFoundException.class)
                .hasMessageContaining(unknownId.toString());

        verify(profileRepository).findById(unknownId);
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
}