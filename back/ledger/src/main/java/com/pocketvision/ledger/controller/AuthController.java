package com.pocketvision.ledger.controller;

import com.pocketvision.ledger.model.User;
import com.pocketvision.ledger.service.AuthService;
import com.pocketvision.ledger.util.JwtUtils; // Import mới
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
    
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils; // Inject JwtUtils

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request.fullName(), request.email(), request.password());
            // Tạo token ngay khi đăng ký (để user tự động login luôn) - lưu user ID và role vào token
            String token = jwtUtils.generateToken(user.getEmail(), user.getId(), user.getRole().name());
            
            return ResponseEntity.ok(new AuthResponse(
                token, // Trả về token
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
            // Tạo token khi đăng nhập thành công - lưu user ID và role vào token
            String token = jwtUtils.generateToken(user.getEmail(), user.getId(), user.getRole().name());

            return ResponseEntity.ok(new AuthResponse(
                token, 
                new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.getAvatarUrl())
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.email());
            // Luôn trả về thành công để tránh email enumeration attack
            return ResponseEntity.ok(Map.of(
                "message", "Nếu email tồn tại, chúng tôi đã gửi link đặt lại mật khẩu đến email của bạn."
            ));
        } catch (Exception e) {
            // Vẫn trả về thành công để bảo mật
            return ResponseEntity.ok(Map.of(
                "message", "Nếu email tồn tại, chúng tôi đã gửi link đặt lại mật khẩu đến email của bạn."
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            if (request.token() == null || request.token().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Token không được để trống"));
            }
            if (request.newPassword() == null || request.newPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu mới phải có ít nhất 6 ký tự"));
            }

            authService.resetPassword(request.token(), request.newPassword());
            return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    // Các DTO (Data Transfer Object)
    record RegisterRequest(String fullName, String email, String password) {}
    record LoginRequest(String email, String password) {}
    record ForgotPasswordRequest(String email) {}
    record ResetPasswordRequest(String token, String newPassword) {}
    record UserResponse(Long id, String fullName, String email, String role, String avatarUrl) {}
    // DTO trả về mới bao gồm Token
    record AuthResponse(String accessToken, UserResponse user) {}
}
