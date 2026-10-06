package com.dando.pacepilot.trainingplan.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
}