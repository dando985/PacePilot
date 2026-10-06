package com.dando.pacepilot.trainingplan.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.time.LocalDate;

@Entity
@Table(name = "training_weeks")
public class TrainingWeekEntity {

    @EmbeddedId
    private TrainingWeekId id;

    @MapsId("planId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private TrainingPlanEntity plan;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @OneToMany(
            mappedBy = "week",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("positionInWeek ASC")
    private List<PlannedWorkoutEntity> workouts = new ArrayList<>();

    protected TrainingWeekEntity() {
    }

    public TrainingWeekEntity(
            TrainingPlanEntity plan,
            int weekNumber,
            LocalDate startDate
    ) {
        this.id = new TrainingWeekId(plan.getId(), weekNumber);
        this.plan = plan;
        this.startDate = startDate;
    }

    public TrainingWeekId getId() {
        return id;
    }

    public TrainingPlanEntity getPlan() {
        return plan;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public List<PlannedWorkoutEntity> getWorkouts() {
        return Collections.unmodifiableList(workouts);
    }

    public void addWorkout(PlannedWorkoutEntity workout) {
        if (workout == null || workout.getWeek() != this) {
            throw new IllegalArgumentException("Workout must reference this training week.");
        }

        workouts.add(workout);
    }
}