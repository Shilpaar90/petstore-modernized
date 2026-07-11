package com.example.petstore.catalog.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Supplies the current request path to every {@link CatalogViewController} view as
 * {@code currentPath}. Thymeleaf 3.1 removed the {@code #request}/{@code #httpServletRequest}
 * expression objects, so the language-switch bar (which re-links to the current page with a
 * {@code ?lang=} parameter) gets the path from the model instead.
 */
@ControllerAdvice(assignableTypes = CatalogViewController.class)
public class CatalogViewAdvice {

    @ModelAttribute("currentPath")
    String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
