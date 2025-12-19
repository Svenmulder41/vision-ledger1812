package com.pocketvision.ledger.controller;

import com.pocketvision.ledger.model.Notification;
import com.pocketvision.ledger.service.NotificationService;
import com.pocketvision.ledger.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    // 1. Lấy danh sách thông báo
    @GetMapping
    public ResponseEntity<?> getUserNotifications(@RequestParam Long userId) {
        try {
            securityUtils.validateUserId(userId); // Bảo mật: kiểm tra userId
            return ResponseEntity.ok(notificationService.getUserNotifications(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // 2. Đếm số lượng chưa đọc
    @GetMapping("/unread-count")
    public ResponseEntity<?> countUnread(@RequestParam Long userId) {
        try {
            securityUtils.validateUserId(userId); // Bảo mật: kiểm tra userId
            return ResponseEntity.ok(notificationService.countUnread(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // 3. Đánh dấu 1 thông báo là Đã đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // 4. Đánh dấu TẤT CẢ là Đã đọc
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@RequestParam Long userId) {
        try {
            securityUtils.validateUserId(userId); // Bảo mật: kiểm tra userId
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // 5. Xóa thông báo
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }
}