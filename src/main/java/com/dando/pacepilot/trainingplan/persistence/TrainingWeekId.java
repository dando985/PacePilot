package com.dando.pacepilot.trainingplan.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class TrainingWeekId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    public TrainingWeekId() {
    }

    public TrainingWeekId(UUID planId, int weekNumber) {
        this.planId = planId;
        this.weekNumber = weekNumber;
    }

    public UUID getPlanId() {
        return planId;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof TrainingWeekId that)) {
            return false;
        }

        return weekNumber == that.weekNumber
                && Objects.equals(planId, that.planId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, weekNumber);
    }
}