package com.dando.pacepilot.account.security;

import com.dando.pacepilot.account.persistence.UserAccountEntity;
import com.dando.pacepilot.account.persistence.UserAccountJpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final UserAccountJpaRepository accountRepository;

    public AccountUserDetailsService(UserAccountJpaRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Invalid email or password.");
        }

        UserAccountEntity account = accountRepository
                .findByEmailIgnoreCase(email.strip())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password."));

        return User.withUsername(account.getEmail())
                .password(account.getPasswordHash())
                .roles("USER")
                .build();
    }
}