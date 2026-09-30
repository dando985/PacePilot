package com.dando.pacepilot.athlete.persistence;

import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.domain.DistanceUnit;
import com.dando.pacepilot.athlete.domain.ExperienceLevel;
import com.dando.pacepilot.athlete.domain.FitnessGoal;
import com.dando.pacepilot.athlete.domain.GoalType;
import com.dando.pacepilot.athlete.repository.AthleteProfileRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
class AthleteProfilePersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18.6-alpine")
                    .withDatabaseName("pacepilot_test")
                    .withUsername("test")
                    .withPassword("test");

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