package com.dando.pacepilot.athlete.persistence;

import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.shared.domain.DistanceUnit;
import com.dando.pacepilot.athlete.domain.ExperienceLevel;
import com.dando.pacepilot.athlete.domain.FitnessGoal;
import com.dando.pacepilot.athlete.domain.GoalType;
import com.dando.pacepilot.athlete.repository.AthleteProfileRepository;
import com.dando.pacepilot.TestcontainersConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AthleteProfilePersistenceIntegrationTest {

    @Autowired
    private AthleteProfileRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndReadsAthleteProfile() {
        UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");

        AthleteProfile profile = new AthleteProfile(
                id,
                "Integration Test Athlete",
                ExperienceLevel.BEGINNER,
                4,
                15.0,
                DistanceUnit.MILES,
                new FitnessGoal(
                        GoalType.MARATHON,
                        LocalDate.of(2027, 3, 21),
                        240
                )
        );

        AthleteProfile savedProfile = repository.save(profile);

        entityManager.flush();
        entityManager.clear();

        AthleteProfile retrievedProfile = repository.findById(id).orElseThrow();

        assertThat(savedProfile).isEqualTo(profile);
        assertThat(retrievedProfile).isEqualTo(profile);
    }
}