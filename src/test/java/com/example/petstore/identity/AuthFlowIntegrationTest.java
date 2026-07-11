package com.example.petstore.identity;

import com.example.petstore.identity.application.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end identity flow against the real Spring Security stack, BCrypt encoder, and JPA user
 * store: register → the credential is persisted (hashed) → form login authenticates → the secured
 * account page is reachable, and unauthenticated access is redirected to login.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    @Test
    void registerThenLoginThenReachAccount() throws Exception {
        mvc.perform(post("/register")
                        .param("username", "alice")
                        .param("password", "secret123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        assertThat(users.existsByUsername("alice")).isTrue();
        // Password is stored hashed, never in plaintext.
        assertThat(users.findByUsername("alice").orElseThrow().passwordHash())
                .isNotEqualTo("secret123")
                .startsWith("$2");

        mvc.perform(formLogin("/login").user("alice").password("secret123"))
                .andExpect(authenticated().withUsername("alice"));

        mvc.perform(get("/account").with(user("alice")))
                .andExpect(status().isOk());
    }

    @Test
    void wrongPasswordDoesNotAuthenticate() throws Exception {
        mvc.perform(post("/register")
                        .param("username", "carol")
                        .param("password", "secret123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        mvc.perform(formLogin("/login").user("carol").password("wrong-password"))
                .andExpect(unauthenticated());
    }

    @Test
    void accountRequiresAuthentication() throws Exception {
        mvc.perform(get("/account"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void catalogBrowsingIsPublic() throws Exception {
        mvc.perform(get("/categories")).andExpect(status().isOk());
        mvc.perform(get("/api/catalog/categories")).andExpect(status().isOk());
    }
}
