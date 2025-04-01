package com.hedera.tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for authentication-related views
 */
@Controller
public class AuthViewController {

    /**
     * Display login page
     * @return the login view
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Display registration page
     * @return the register view
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }
} 