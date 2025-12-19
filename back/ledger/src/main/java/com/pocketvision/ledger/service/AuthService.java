package com.pocketvision.ledger.service;

import com.pocketvision.ledger.model.Notification;
import com.pocketvision.ledger.model.User;
import com.pocketvision.ledger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    // --- ĐĂNG KÝ ---
    public User register(String fullName, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(User.UserRole.USER);

        User savedUser = userRepository.save(user);

        // Gửi thông báo chào mừng
        try {
            notificationService.createNotification(
                savedUser.getId(),
                "Xin chào thành viên mới!",
                "Chào mừng " + fullName + "! Hãy bắt đầu quản lý tài chính ngay.",
                Notification.NotificationType.GENERAL,
                null
            );
        } catch (Exception e) {
            // Log lỗi nhưng không chặn luồng chính
            System.err.println("Lỗi tạo thông báo: " + e.getMessage());
        }

        return savedUser;
    }

    // --- ĐĂNG NHẬP ---
    public User login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Email không tồn tại!");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu không đúng!");
        }

        return user;
    }
}
