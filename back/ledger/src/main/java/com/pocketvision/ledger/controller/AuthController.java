package com.pocketvision.ledger.controller;

import com.pocketvision.ledger.model.User;
import com.pocketvision.ledger.service.AuthService;
import com.pocketvision.ledger.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// Đã xóa @CrossOrigin để fix lỗi 403 trên Render
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request.fullName(), request.email(), request.password());
            // Tạo token đơn giản (1 tham số) để an toàn nhất lúc này
            String token = jwtUtils.generateToken(user.getEmail());
            
            return ResponseEntity.ok(new AuthResponse(
                token, 
                new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.getAvatarUrl())
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = authService.login(request.email(), request.password());
            String token = jwtUtils.generateToken(user.getEmail());

            return ResponseEntity.ok(new AuthResponse(
                token, 
                new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.getAvatarUrl())
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // DTO rút gọn
    record RegisterRequest(String fullName, String email, String password) {}
    record LoginRequest(String email, String password) {}
    record UserResponse(Long id, String fullName, String email, String role, String avatarUrl) {}
    record AuthResponse(String accessToken, UserResponse user) {}
}
