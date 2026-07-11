package com.example.petstore.identity.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Form-backing bean for the registration page. Mutable (getters/setters) so the Spring MVC data
 * binder and Thymeleaf can populate it; validated with Bean Validation.
 */
public class RegistrationForm {

    @NotBlank
    @Size(min = 3, max = 80)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
