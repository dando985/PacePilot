package com.dando.pacepilot.trainingplan.persistence;

import com.dando.pacepilot.shared.domain.DistanceUnit;
import com.dando.pacepilot.trainingplan.domain.GoalType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "training_plans")
public class TrainingPlanEntity {

    @Id
    private UUID id;

    @Column(name = "athlete_profile_id", nullable = false)
    private UUID athleteProfileId;

    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 30)
    private GoalType goalType;

    @Column(name = "goal_target_date", nullable = false)
    private LocalDate goalTargetDate;

    @Column(name = "goal_target_time_minutes")
    private Integer goalTargetTimeMinutes;

    @Column(name = "available_training_days_per_week", nullable = false)
    private int availableTrainingDaysPerWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance_unit", nullable = false, length = 20)
    private DistanceUnit distanceUnit;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id.weekNumber ASC")
    private List<TrainingWeekEntity> weeks = new ArrayList<>();

    protected TrainingPlanEntity() {
    }

    public TrainingPlanEntity(
            UUID id,
            UUID athleteProfileId,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            GoalType goalType,
            LocalDate goalTargetDate,
            Integer goalTargetTimeMinutes,
            int availableTrainingDaysPerWeek,
            DistanceUnit distanceUnit
    ) {
        this.id = id;
        this.athleteProfileId = athleteProfileId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.goalType = goalType;
        this.goalTargetDate = goalTargetDate;
        this.goalTargetTimeMinutes = goalTargetTimeMinutes;
        this.availableTrainingDaysPerWeek = availableTrainingDaysPerWeek;
        this.distanceUnit = distanceUnit;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAthleteProfileId() {
        return athleteProfileId;
    }

    public String getName() {
        return name;
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Training plan name is required.");
        }

        this.name = newName.strip();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public LocalDate getGoalTargetDate() {
        return goalTargetDate;
    }

    public Integer getGoalTargetTimeMinutes() {
        return goalTargetTimeMinutes;
    }

    public int getAvailableTrainingDaysPerWeek() {
        return availableTrainingDaysPerWeek;
    }

    public DistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    public List<TrainingWeekEntity> getWeeks() {
        return Collections.unmodifiableList(weeks);
    }

    public void addWeek(TrainingWeekEntity week) {
        if (week == null || week.getPlan() != this) {
            throw new IllegalArgumentException("Week must reference this training plan.");
        }

        weeks.add(week);
    }
}