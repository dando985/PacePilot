package com.dando.pacepilot.trainingplan.persistence;

import com.dando.pacepilot.shared.domain.DistanceUnit;
import com.dando.pacepilot.trainingplan.domain.WorkoutType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "planned_workouts")
public class PlannedWorkoutEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(
                    name = "plan_id",
                    referencedColumnName = "plan_id",
                    nullable = false
            ),
            @JoinColumn(
                    name = "week_number",
                    referencedColumnName = "week_number",
                    nullable = false
            )
    })
    private TrainingWeekEntity week;

    @Column(name = "position_in_week", nullable = false)
    private int positionInWeek;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "workout_type", nullable = false, length = 30)
    private WorkoutType type;

    @Column(name = "title", nullable = false, columnDefinition = "text")
    private String title;

    @Column(name = "target_distance")
    private Double targetDistance;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance_unit", length = 20)
    private DistanceUnit distanceUnit;

    @Column(name = "target_duration_minutes")
    private Integer targetDurationMinutes;

    @Column(name = "instructions", nullable = false, columnDefinition = "text")
    private String instructions;

    protected PlannedWorkoutEntity() {
    }

    public PlannedWorkoutEntity(
            UUID id,
            TrainingWeekEntity week,
            int positionInWeek,
            LocalDate scheduledDate,
            WorkoutType type,
            String title,
            Double targetDistance,
            DistanceUnit distanceUnit,
            Integer targetDurationMinutes,
            String instructions
    ) {
        this.id = id;
        this.week = week;
        this.positionInWeek = positionInWeek;
        this.scheduledDate = scheduledDate;
        this.type = type;
        this.title = title;
        this.targetDistance = targetDistance;
        this.distanceUnit = distanceUnit;
        this.targetDurationMinutes = targetDurationMinutes;
        this.instructions = instructions;
    }

    public UUID getId() {
        return id;
    }

    public TrainingWeekEntity getWeek() {
        return week;
    }

    public int getPositionInWeek() {
        return positionInWeek;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public WorkoutType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public Double getTargetDistance() {
        return targetDistance;
    }

    public DistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    public Integer getTargetDurationMinutes() {
        return targetDurationMinutes;
    }

    public String getInstructions() {
        return instructions;
    }
}