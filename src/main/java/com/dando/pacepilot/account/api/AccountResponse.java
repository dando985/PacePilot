package com.dando.pacepilot.account.api;

import com.dando.pacepilot.account.persistence.UserAccountEntity;

import java.time.Instant;
import java.util.UUID;

// defines the safe account metadata returned to frontend
public record AccountResponse(UUID id, String email, Instant createdAt) {

    public static AccountResponse from(UserAccountEntity account) {
        return new AccountResponse(account.getId(), account.getEmail(), account.getCreatedAt());
    }
}