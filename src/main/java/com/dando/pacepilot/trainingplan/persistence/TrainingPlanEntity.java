package com.dando.pacepilot.trainingplan.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.time.LocalDate;
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

    @OneToMany(
            mappedBy = "plan",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id.weekNumber ASC")
    private List<TrainingWeekEntity> weeks = new ArrayList<>();

    protected TrainingPlanEntity() {
    }

    public TrainingPlanEntity(
            UUID id,
            UUID athleteProfileId,
            String name,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.id = id;
        this.athleteProfileId = athleteProfileId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
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