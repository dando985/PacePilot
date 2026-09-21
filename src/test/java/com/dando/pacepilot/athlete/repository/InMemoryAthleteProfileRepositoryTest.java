package com.dando.pacepilot.athlete.repository;

import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.domain.DistanceUnit;
import com.dando.pacepilot.athlete.domain.ExperienceLevel;
import com.dando.pacepilot.athlete.domain.FitnessGoal;
import com.dando.pacepilot.athlete.domain.GoalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryAthleteProfileRepositoryTest {

    private InMemoryAthleteProfileRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAthleteProfileRepository();
    }

    @Test
    void savesAndFindsProfileById() {
        UUID id = UUID.randomUUID();
        AthleteProfile profile = createProfile(id, "Dan");

        AthleteProfile savedProfile = repository.save(profile);

        assertThat(savedProfile).isEqualTo(profile);

        assertThat(repository.findById(id)).contains(profile);
    }

    @Test
    void returnsEmptyWhenProfileDoesNotExist() {
        UUID unknownId = UUID.randomUUID();

        assertThat(repository.findById(unknownId)).isEmpty();
    }

    @Test
    void returnsAllSavedProfiles() {
        AthleteProfile firstProfile = createProfile(UUID.randomUUID(), "Dan");
        AthleteProfile secondProfile = createProfile(UUID.randomUUID(), "Taylor");

        repository.save(firstProfile);
        repository.save(secondProfile);

        assertThat(repository.findAll()).contains(firstProfile, secondProfile);
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