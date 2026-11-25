package com.dando.pacepilot.athlete.service;

import com.dando.pacepilot.account.service.UserAccountService;
import com.dando.pacepilot.athlete.api.CreateAthleteProfileRequest;
import com.dando.pacepilot.athlete.api.UpdateAthleteProfileRequest;
import com.dando.pacepilot.athlete.domain.AthleteProfile;
import com.dando.pacepilot.athlete.exception.AthleteProfileNotFoundException;
import com.dando.pacepilot.athlete.repository.AthleteProfileRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AthleteProfileService {

    private static final String DUPLICATE_PROFILE_MESSAGE = "This account already has an athlete profile.";

    private final AthleteProfileRepository profileRepository;
    private final UserAccountService accountService;

    public AthleteProfileService(AthleteProfileRepository profileRepository, UserAccountService accountService) {
        this.profileRepository = profileRepository;
        this.accountService = accountService;
    }

    @Transactional
    public AthleteProfile createProfile(CreateAthleteProfileRequest request) {
        UUID ownerAccountId = accountService.getCurrentAccount().id();

        // only allow one profile per account
        if (profileRepository.findByOwnerAccountId(ownerAccountId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_PROFILE_MESSAGE);
        }

        AthleteProfile profile = new AthleteProfile(
                UUID.randomUUID(),
                request.displayName(),
                request.birthDate(),
                request.weight(),
                request.weightUnit(),
                request.gender(),
                request.experienceLevel()
        );

        try {
            return profileRepository.save(profile, ownerAccountId);
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateOwner(exception)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_PROFILE_MESSAGE, exception);
            }

            throw exception;
        }
    }

    @Transactional
    public AthleteProfile updateProfile(UUID id, UpdateAthleteProfileRequest request) {
        UUID ownerAccountId = accountService.getCurrentAccount().id();
        AthleteProfile existingProfile = findOwnedProfile(id, ownerAccountId);

        AthleteProfile updatedProfile = new AthleteProfile(
                existingProfile.id(),
                request.displayName(),
                request.birthDate(),
                request.weight(),
                request.weightUnit(),
                request.gender(),
                request.experienceLevel()
        );

        return profileRepository.save(updatedProfile, ownerAccountId);
    }

    @Transactional(readOnly = true)
    public AthleteProfile getProfileById(UUID id) {
        UUID ownerAccountId = accountService.getCurrentAccount().id();

        return findOwnedProfile(id, ownerAccountId);
    }

    @Transactional(readOnly = true)
    public AthleteProfile getCurrentProfile() {
        UUID ownerAccountId = accountService.getCurrentAccount().id();

        return profileRepository
                .findByOwnerAccountId(ownerAccountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No athlete profile has been created for this account."
                ));
    }

    // helper to check that the account ID matches with the profile ID
    private AthleteProfile findOwnedProfile(UUID id, UUID ownerAccountId) {
        return profileRepository
                .findByIdAndOwnerAccountId(id, ownerAccountId)
                .orElseThrow(() -> new AthleteProfileNotFoundException(id));
    }

    // helper to ensure that there is only one profile per account
    private boolean isDuplicateOwner(DataIntegrityViolationException exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation
                    && "athlete_profiles_owner_account_unique".equals(violation.getConstraintName())) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
}