package be.kicksync_backend.controller;

import be.kicksync_backend.dto.AuthResponse;
import be.kicksync_backend.dto.LoginRequest;
import be.kicksync_backend.dto.RegisterRequest;
import be.kicksync_backend.entity.User;
import be.kicksync_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request.getUsername(), request.getEmail(), request.getPassword());
            String token = authService.login(request.getUsername(), request.getPassword());
            
            AuthResponse response = new AuthResponse(token, user.getUsername(), user.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.getUsername(), request.getPassword());
            User user = authService.getUserByUsername(request.getUsername());
            
            AuthResponse response = new AuthResponse(token, user.getUsername(), user.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}