package com.example.petstore.support.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Landing page for the storefront. This is the "walking skeleton" endpoint: it proves the
 * Spring MVC + Thymeleaf view pipeline is wired end-to-end before any domain slice is migrated.
 */
@Controller
public class HomeController {

    private final String appName;

    public HomeController(@Value("${spring.application.name}") String appName) {
        this.appName = appName;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appName", appName);
        return "index";
    }
}
