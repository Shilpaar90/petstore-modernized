package com.example.petstore.identity.adapter.in.web;

import com.example.petstore.identity.application.UsernameTakenException;
import com.example.petstore.identity.application.port.in.RegisterUser;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Inbound web adapter for authentication: the custom login page, self-service registration, and
 * the secured account landing page. Login itself is handled by Spring Security's filter (POST
 * {@code /login}); this controller only renders the form.
 */
@Controller
public class AuthController {

    private final RegisterUser registerUser;

    public AuthController(RegisterUser registerUser) {
        this.registerUser = registerUser;
    }

    @GetMapping("/login")
    public String login() {
        return "identity/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "identity/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult binding) {
        if (binding.hasErrors()) {
            return "identity/register";
        }
        try {
            registerUser.register(form.getUsername(), form.getPassword());
        } catch (UsernameTakenException e) {
            binding.rejectValue("username", "taken", "That username is already taken.");
            return "identity/register";
        }
        // Registered — send to the login page with a success flag.
        return "redirect:/login?registered";
    }

    @GetMapping("/account")
    public String account() {
        return "identity/account";
    }
}
