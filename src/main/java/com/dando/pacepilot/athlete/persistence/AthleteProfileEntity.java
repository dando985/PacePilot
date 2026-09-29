package com.dando.pacepilot.athlete.persistence;

import com.dando.pacepilot.athlete.domain.DistanceUnit;
import com.dando.pacepilot.athlete.domain.ExperienceLevel;
import com.dando.pacepilot.athlete.domain.GoalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "athlete_profiles")
public class AthleteProfileEntity {

    @Id
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", nullable = false, length = 20)
    private ExperienceLevel experienceLevel;

    @Column(name = "available_training_days_per_week", nullable = false)
    private int availableTrainingDaysPerWeek;

    @Column(name = "current_weekly_running_distance")
    private Double currentWeeklyRunningDistance;

    @Enumerated(EnumType.STRING)
    @Column(name = "running_distance_unit", length = 20)
    private DistanceUnit runningDistanceUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 30)
    private GoalType goalType;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "target_time_minutes")
    private Integer targetTimeMinutes;

    protected AthleteProfileEntity() {
    }

    public AthleteProfileEntity(
            UUID id,
            String displayName,
            ExperienceLevel experienceLevel,
            int availableTrainingDaysPerWeek,
            Double currentWeeklyRunningDistance,
            DistanceUnit runningDistanceUnit,
            GoalType goalType,
            LocalDate targetDate,
            Integer targetTimeMinutes
    ) {
        this.id = id;
        this.displayName = displayName;
        this.experienceLevel = experienceLevel;
        this.availableTrainingDaysPerWeek = availableTrainingDaysPerWeek;
        this.currentWeeklyRunningDistance = currentWeeklyRunningDistance;
        this.runningDistanceUnit = runningDistanceUnit;
        this.goalType = goalType;
        this.targetDate = targetDate;
        this.targetTimeMinutes = targetTimeMinutes;
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public int getAvailableTrainingDaysPerWeek() {
        return availableTrainingDaysPerWeek;
    }

    public Double getCurrentWeeklyRunningDistance() {
        return currentWeeklyRunningDistance;
    }

    public DistanceUnit getRunningDistanceUnit() {
        return runningDistanceUnit;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public Integer getTargetTimeMinutes() {
        return targetTimeMinutes;
    }
}