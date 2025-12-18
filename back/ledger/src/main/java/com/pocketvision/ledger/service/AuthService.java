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

        // --- GỬI THÔNG BÁO (Đã sửa: Thêm tiêu đề "Xin chào") ---
        try {
            notificationService.createNotification(
                savedUser.getId(),
                "Xin chào thành viên mới!", // Title (Tham số thứ 2)
                "Chào mừng " + fullName + "! Hãy bắt đầu quản lý tài chính thông minh ngay hôm nay.", // Message (Tham số thứ 3)
                Notification.NotificationType.GENERAL, // Type (Tham số thứ 4)
                null // RelatedId (Tham số thứ 5)
            );
        } catch (Exception e) {
            System.err.println("Lỗi gửi thông báo chào mừng: " + e.getMessage());
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