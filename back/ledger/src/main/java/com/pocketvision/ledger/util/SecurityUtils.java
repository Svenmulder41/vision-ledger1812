package com.pocketvision.ledger.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.pocketvision.ledger.config.CustomUserDetails;
import com.pocketvision.ledger.exception.SecurityException;
import com.pocketvision.ledger.model.User;
import com.pocketvision.ledger.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Lấy email của user hiện tại đang đăng nhập
     */
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("Người dùng chưa được xác thực");
        }
        return auth.getName();
    }

    /**
     * Lấy User ID của user hiện tại đang đăng nhập
     * Tối ưu: Lấy từ CustomUserDetails nếu có, không thì mới query database
     */
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("Người dùng chưa được xác thực");
        }
        
        // Tối ưu: Lấy user ID từ CustomUserDetails nếu có (tránh query database)
        if (auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            return userDetails.getUserId();
        }
        
        // Fallback: Query database nếu không có user ID trong authentication
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Không tìm thấy người dùng với email: " + email));
        return user.getId();
    }

    /**
     * Kiểm tra xem userId có phải là của user hiện tại không
     * @param userId User ID cần kiểm tra
     * @throws SecurityException nếu userId không khớp với user hiện tại
     */
    public void validateUserId(Long userId) {
        if (userId == null) {
            throw new SecurityException("Thiếu thông tin userId");
        }
        
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new SecurityException("Bạn không có quyền truy cập dữ liệu của người dùng khác");
        }
    }
}

