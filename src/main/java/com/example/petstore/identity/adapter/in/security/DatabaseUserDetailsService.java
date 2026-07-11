package com.example.petstore.identity.adapter.in.security;

import com.example.petstore.identity.application.StoredUser;
import com.example.petstore.identity.application.port.out.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridges Spring Security's authentication to the {@link UserRepository} port: the framework's
 * {@code DaoAuthenticationProvider} calls this to load credentials, then verifies the submitted
 * password against the stored BCrypt hash. Replaces the legacy {@code SignOnFilter}.
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public DatabaseUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        StoredUser user = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return User.withUsername(user.username())
                .password(user.passwordHash())
                .disabled(!user.enabled())
                .roles("USER")
                .build();
    }
}
