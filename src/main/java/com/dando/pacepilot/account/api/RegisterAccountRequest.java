package com.dando.pacepilot.account.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;

public record RegisterAccountRequest(

        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be a valid address.")
        @Size(max = 254, message = "Email cannot exceed 254 characters.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 6, max = 72, message = "Password must contain between 6 and 72 characters.")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {

    // ensure password is within BCrypt's 72-byte input limit
    @AssertTrue(message = "Password cannot exceed 72 bytes when encoded as UTF-8.")
    @JsonIgnore
    public boolean isPasswordWithinByteLimit() {
        return password == null || (password.length() <= 72 && password.getBytes(StandardCharsets.UTF_8).length <= 72);
    }

    @Override
    public String toString() {
        return "RegisterAccountRequest[email=" + email + ", password=[REDACTED]]";
    }
}