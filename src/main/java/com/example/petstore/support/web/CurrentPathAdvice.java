package com.example.petstore.support.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Supplies the current request path to every view as {@code currentPath}. The shared layout's
 * language-switch bar re-links to the current page with a {@code ?lang=} parameter, and Thymeleaf
 * 3.1 removed the {@code #request}/{@code #httpServletRequest} expression objects — so the path
 * comes from the model instead. Applied storefront-wide since the top bar appears on every page.
 */
@ControllerAdvice
public class CurrentPathAdvice {

    @ModelAttribute("currentPath")
    String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
