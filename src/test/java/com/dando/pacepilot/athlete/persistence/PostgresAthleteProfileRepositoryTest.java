package com.dando.pacepilot.athlete.persistence;

import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.shared.domain.DistanceUnit;
import com.dando.pacepilot.athlete.domain.ExperienceLevel;
import com.dando.pacepilot.athlete.domain.FitnessGoal;
import com.dando.pacepilot.athlete.domain.GoalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgresAthleteProfileRepositoryTest {

    private static final UUID PROFILE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private AthleteProfileJpaRepository jpaRepository;

    private PostgresAthleteProfileRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PostgresAthleteProfileRepository(jpaRepository);
    }

    @Test
    void savesProfileAsEntityAndReturnsDomainProfile() {
        AthleteProfile profile = createProfile();

        when(jpaRepository.save(any(AthleteProfileEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // create a captor to capture local AthleteProfileEntity object from the save call above
        ArgumentCaptor<AthleteProfileEntity> entityCaptor = ArgumentCaptor.forClass(AthleteProfileEntity.class);

        AthleteProfile savedProfile = repository.save(profile);

        // retrieve those recorded arguments from the save method call and verify that the save method was called with the correct arguments
        verify(jpaRepository).save(entityCaptor.capture());

        // extract the AthleteProfileEntity object from the captor
        AthleteProfileEntity capturedEntity = entityCaptor.getValue();

        // check that the AthleteProfileEntity object is mapped correctly to the AthleteProfile domain
        assertThat(capturedEntity.getId()).isEqualTo(profile.id());
        assertThat(capturedEntity.getDisplayName()).isEqualTo(profile.displayName());
        assertThat(capturedEntity.getExperienceLevel()).isEqualTo(profile.experienceLevel());
        assertThat(capturedEntity.getAvailableTrainingDaysPerWeek()).isEqualTo(profile.availableTrainingDaysPerWeek());
        assertThat(capturedEntity.getCurrentWeeklyRunningDistance()).isEqualTo(profile.currentWeeklyRunningDistance());
        assertThat(capturedEntity.getRunningDistanceUnit()).isEqualTo(profile.runningDistanceUnit());
        assertThat(capturedEntity.getGoalType()).isEqualTo(profile.goal().type());
        assertThat(capturedEntity.getTargetDate()).isEqualTo(profile.goal().targetDate());
        assertThat(capturedEntity.getTargetTimeMinutes()).isEqualTo(profile.goal().targetTimeMinutes());
        assertThat(savedProfile).isEqualTo(profile);
    }

    @Test
    void findsProfileByIdAndConvertsItToDomainProfile() {
        AthleteProfile expectedProfile = createProfile();
        AthleteProfileEntity entity = createEntity();

        when(jpaRepository.findById(PROFILE_ID)).thenReturn(Optional.of(entity));

        Optional<AthleteProfile> result = repository.findById(PROFILE_ID);

        assertThat(result).contains(expectedProfile);
    }

    @Test
    void convertsEveryEntityReturnedByFindAll() {
        AthleteProfile expectedProfile = createProfile();
        AthleteProfileEntity entity = createEntity();

        when(jpaRepository.findAll()).thenReturn(List.of(entity));

        List<AthleteProfile> result = repository.findAll();

        assertThat(result).containsExactly(expectedProfile);
    }

    // helper to create sample athlete profile domain
    private static AthleteProfile createProfile() {
        FitnessGoal goal = new FitnessGoal(
                GoalType.MARATHON,
                LocalDate.of(2027, 3, 21),
                240
        );

        return new AthleteProfile(
                PROFILE_ID,
                "Dan",
                ExperienceLevel.BEGINNER,
                4,
                15.0,
                DistanceUnit.MILES,
                goal
        );
    }

    // helper to create athlete profile entity
    private static AthleteProfileEntity createEntity() {
        return new AthleteProfileEntity(
                PROFILE_ID,
                "Dan",
                ExperienceLevel.BEGINNER,
                4,
                15.0,
                DistanceUnit.MILES,
                GoalType.MARATHON,
                LocalDate.of(2027, 3, 21),
                240
        );
    }
}