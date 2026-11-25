package com.dando.pacepilot.athlete.persistence;

import com.dando.pacepilot.athlete.domain.ExperienceLevel;
import com.dando.pacepilot.athlete.domain.Gender;
import com.dando.pacepilot.shared.domain.WeightUnit;
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

    @Column(name = "owner_account_id", nullable = false, updatable = false)
    private UUID ownerAccountId;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_unit", nullable = false, length = 20)
    private WeightUnit weightUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", nullable = false, length = 20)
    private ExperienceLevel experienceLevel;

    protected AthleteProfileEntity() {
    }

    public AthleteProfileEntity(
            UUID id,
            UUID ownerAccountId,
            String displayName,
            LocalDate birthDate,
            Double weight,
            WeightUnit weightUnit,
            Gender gender,
            ExperienceLevel experienceLevel
    ) {
        this.id = id;
        this.ownerAccountId = ownerAccountId;
        this.displayName = displayName;
        this.birthDate = birthDate;
        this.weight = weight;
        this.weightUnit = weightUnit;
        this.gender = gender;
        this.experienceLevel = experienceLevel;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerAccountId() {
        return ownerAccountId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Double getWeight() {
        return weight;
    }

    public WeightUnit getWeightUnit() {
        return weightUnit;
    }

    public Gender getGender() {
        return gender;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }
}