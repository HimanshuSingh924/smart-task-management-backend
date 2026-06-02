package com.taskmanager.auth.config;

import com.taskmanager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The ONLY class required by the jwt-auth-starter.
 *
 * This service is auto-detected by the starter's JwtAuthenticationFilter
 * when it needs to load a user by email (username) from the database.
 *
 * The starter's @ConditionalOnMissingBean means it will use this implementation
 * instead of the default no-op DefaultUserDetailsService.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by email. Called by JwtAuthenticationFilter on every request
     * after successfully extracting a username from the JWT token.
     *
     * @param email the email stored as JWT subject
     * @throws UsernameNotFoundException if no user matches the email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.taskmanager.users.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
