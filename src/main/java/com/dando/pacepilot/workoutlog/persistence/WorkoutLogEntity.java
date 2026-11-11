package com.dando.pacepilot.workoutlog.persistence;

import com.dando.pacepilot.shared.domain.DistanceUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "workout_logs",
        uniqueConstraints = @UniqueConstraint(
                name = "workout_logs_planned_workout_unique",
                columnNames = {"planned_workout_id"}
        )
)
public class WorkoutLogEntity {

    @Id
    private UUID id;

    @Column(name = "athlete_profile_id", nullable = false)
    private UUID athleteProfileId;

    @Column(name = "planned_workout_id", nullable = false)
    private UUID plannedWorkoutId;

    @Column(name = "completed_date", nullable = false)
    private LocalDate completedDate;

    @Column(name = "actual_distance")
    private Double actualDistance;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance_unit", length = 20)
    private DistanceUnit distanceUnit;

    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    protected WorkoutLogEntity() {
    }

    public WorkoutLogEntity(
            UUID id,
            UUID athleteProfileId,
            UUID plannedWorkoutId,
            LocalDate completedDate,
            Double actualDistance,
            DistanceUnit distanceUnit,
            Integer actualDurationMinutes,
            String notes
    ) {
        this.id = id;
        this.athleteProfileId = athleteProfileId;
        this.plannedWorkoutId = plannedWorkoutId;
        this.completedDate = completedDate;
        this.actualDistance = actualDistance;
        this.distanceUnit = distanceUnit;
        this.actualDurationMinutes = actualDurationMinutes;
        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAthleteProfileId() {
        return athleteProfileId;
    }

    public UUID getPlannedWorkoutId() {
        return plannedWorkoutId;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public Double getActualDistance() {
        return actualDistance;
    }

    public DistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    public Integer getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public String getNotes() {
        return notes;
    }
}