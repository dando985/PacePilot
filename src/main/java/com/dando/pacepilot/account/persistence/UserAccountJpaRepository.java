package com.dando.pacepilot.account.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, UUID> {

    @Query("""
            select account
            from UserAccountEntity account
            where lower(account.email) = lower(:email)
            """)
    Optional<UserAccountEntity> findByEmailIgnoreCase(@Param("email") String email);
}