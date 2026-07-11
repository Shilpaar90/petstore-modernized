package com.example.petstore.identity.application;

import com.example.petstore.identity.application.port.out.PasswordHasher;
import com.example.petstore.identity.application.port.out.UserRepository;
import com.example.petstore.identity.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordHasher hasher;

    @InjectMocks
    private RegistrationService service;

    @Test
    void registersNewUserWithHashedPassword() {
        when(users.existsByUsername("alice")).thenReturn(false);
        when(hasher.hash("secret123")).thenReturn("$2a$hashed");

        User user = service.register("alice", "secret123");

        assertThat(user).isEqualTo(new User("alice", true));
        verify(hasher).hash("secret123");
        verify(users).create("alice", "$2a$hashed", true);
    }

    @Test
    void trimsUsernameBeforePersisting() {
        when(users.existsByUsername("bob")).thenReturn(false);
        when(hasher.hash(anyString())).thenReturn("h");

        service.register("  bob  ", "secret123");

        verify(users).create(eq("bob"), anyString(), eq(true));
    }

    @Test
    void rejectsDuplicateUsernameWithoutPersisting() {
        when(users.existsByUsername("alice")).thenReturn(true);

        assertThatExceptionOfType(UsernameTakenException.class)
                .isThrownBy(() -> service.register("alice", "secret123"));
        verify(users, never()).create(anyString(), anyString(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void rejectsBlankCredentials() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.register("   ", "secret123"));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.register("alice", "  "));
        verify(users, never()).create(anyString(), anyString(), org.mockito.ArgumentMatchers.anyBoolean());
    }
}
