package com.pocketvision.ledger.service;

import com.pocketvision.ledger.model.Notification;
import com.pocketvision.ledger.model.User;
import com.pocketvision.ledger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
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

    // --- QUÊN MẬT KHẨU ---
    public void forgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Không báo lỗi để tránh email enumeration attack
            return;
        }

        User user = userOpt.get();
        
        // Tạo reset token ngẫu nhiên
        String resetToken = generateResetToken();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1); // Token hết hạn sau 1 giờ

        user.setResetToken(resetToken);
        user.setResetTokenExpiry(expiry);
        userRepository.save(user);

        // TODO: Gửi email với reset token
        // Hiện tại chỉ log ra console để test
        System.out.println("=== RESET PASSWORD TOKEN ===");
        System.out.println("Email: " + email);
        System.out.println("Reset Token: " + resetToken);
        System.out.println("Link: http://localhost:8081/reset-password?token=" + resetToken);
        System.out.println("Token expires at: " + expiry);
        System.out.println("============================");
    }

    // --- ĐẶT LẠI MẬT KHẨU ---
    public void resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Token không hợp lệ hoặc đã hết hạn!");
        }

        User user = userOpt.get();

        // Kiểm tra token có hết hạn không
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            // Xóa token đã hết hạn
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);
            throw new IllegalArgumentException("Token đã hết hạn! Vui lòng yêu cầu lại.");
        }

        // Cập nhật mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // Xóa token sau khi đã dùng
        user.setResetTokenExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // Tạo reset token ngẫu nhiên
    private String generateResetToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}