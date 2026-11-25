package com.chuange.diy.backend.controller;

import com.chuange.diy.backend.model.db.ShopUser;
import com.chuange.diy.backend.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record RegisterRequest(String username, String email, String password,
                                  String name, String phone, String address) {}

    public record LoginRequest(String account, String password) {}

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest req) {
        return authService.register(
                req.username(), req.email(), req.password(),
                req.name(), req.phone(), req.address()
        );
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req) {
        return authService.login(req.account(), req.password());
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        ShopUser user = (authentication != null && authentication.getPrincipal() instanceof ShopUser u) ? u : null;
        return authService.getProfile(user);
    }
}