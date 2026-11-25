package com.dando.pacepilot.account.service;

import com.dando.pacepilot.account.api.AccountResponse;
import com.dando.pacepilot.account.api.RegisterAccountRequest;
import com.dando.pacepilot.account.persistence.UserAccountEntity;
import com.dando.pacepilot.account.persistence.UserAccountJpaRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

// handles account registration and retrieving the currently logged-in account
@Service
@Validated
public class UserAccountService {

    private final UserAccountJpaRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountJpaRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AccountResponse register(@NotNull @Valid RegisterAccountRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        String passwordHash = passwordEncoder.encode(request.password());

        UserAccountEntity account = new UserAccountEntity(
                UUID.randomUUID(),
                email,
                passwordHash,
                Instant.now()
        );

        try {
            UserAccountEntity savedAccount = accountRepository.saveAndFlush(account);
            return AccountResponse.from(savedAccount);
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateEmail(exception)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Unable to register an account with these details.", exception
                );
            }

            throw exception;
        }
    }

    // reads authenticated identity from Spring's security context, looks up account, and returns its safe metadata
    @Transactional(readOnly = true)
    public AccountResponse getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }

        return accountRepository
                .findByEmailIgnoreCase(authentication.getName())
                .map(AccountResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required."));
    }

    // helper to prevent duplicate emails from signing up.
    // examines the exception chain for our specific user_accounts_email_unique constraint
    private boolean isDuplicateEmail(DataIntegrityViolationException exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation
                    && "user_accounts_email_unique".equals(violation.getConstraintName())) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
}