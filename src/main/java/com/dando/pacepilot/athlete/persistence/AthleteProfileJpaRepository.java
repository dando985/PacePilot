package com.dando.pacepilot.athlete.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AthleteProfileJpaRepository extends JpaRepository<AthleteProfileEntity, UUID> {
}