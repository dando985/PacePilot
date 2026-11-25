package com.dando.pacepilot.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

// creates bean for password encoding and bean for filtering HTTP requests by building security rules
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(Customizer.withDefaults())    // keep CSRF protection enabled
                .httpBasic(AbstractHttpConfigurer::disable)     // disable HTTP basic authentication
                .requestCache(AbstractHttpConfigurer::disable)  // 	prevents saving requests for a later login redirect.
                .sessionManagement(session -> session   // allows sessions to be created when needed.
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()   // allow internal error dispatch without login
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()  // allow CSRF token retrieval without login
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()  // allow unauthenticated access to these two post routes
                        .anyRequest().authenticated()   // all remaining requests need an authorized user
                )
                .formLogin(form -> form     // processes email/password login through Spring Security.
                        .loginPage("/api/auth/login")       // sets custom login-page address
                        .loginProcessingUrl("/api/auth/login")  // sets URL where Spring's authentication filter processes submitted credentials
                        .usernameParameter("email")     // read the login identifier from form field "email"
                        .passwordParameter("password")  // read the password from form field "password"
                        .successHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value())
                        )   // returns no content response body (HTTP 204) after successful login
                        .failureHandler((request, response, exception) ->
                                response.setStatus(HttpStatus.UNAUTHORIZED.value())
                        )   // returns unauthorized response body (HTTP 401) after failure to login
                )
                .logout(logout -> logout    // handles logout and session cleanup.
                        .logoutUrl("/api/auth/logout")
                        .deleteCookies("JSESSIONID")    // clear cookie name "JSESSIONID"
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value())
                        )   // return no content response body (HTTP 204) after successful logout
                )
                .exceptionHandling(exceptions -> exceptions     // returns HTTP 401 for unauthorized access
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) ->
                                response.setStatus(HttpStatus.FORBIDDEN.value())
                        )   // a missing or invalid CSRF token may result in HTTP 403 forbidden response
                );

        return http.build();
    }
}