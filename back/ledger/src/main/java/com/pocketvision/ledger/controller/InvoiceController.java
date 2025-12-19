package com.pocketvision.ledger.controller;

import java.util.Map;

import org.springframework.http.HttpStatus; // Import Expense
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pocketvision.ledger.exception.SecurityException;
import com.pocketvision.ledger.model.Expense;
import com.pocketvision.ledger.model.Invoice;
import com.pocketvision.ledger.service.InvoiceService;
import com.pocketvision.ledger.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final SecurityUtils securityUtils;

    // 1. API tải ảnh lên và phân tích
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadAndAnalyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        try {
            securityUtils.validateUserId(userId); // Bảo mật: kiểm tra userId
            Invoice savedInvoice = invoiceService.processAndSaveInvoice(userId, file);
            return ResponseEntity.ok(savedInvoice);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi xử lý hóa đơn: " + e.getMessage());
        }
    }

    // 2. API lấy danh sách hóa đơn
    @GetMapping
    public ResponseEntity<?> getInvoices(@RequestParam Long userId) {
        try {
            securityUtils.validateUserId(userId); // Bảo mật: kiểm tra userId
            return ResponseEntity.ok(invoiceService.getAllInvoices(userId));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // 3. API xóa hóa đơn
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable Long id, @RequestParam Long userId) {
        try {
            securityUtils.validateUserId(userId); // Bảo mật: kiểm tra userId
            invoiceService.deleteInvoice(id, userId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    // 4. API cập nhật hóa đơn (Sửa thông tin, category, payment method)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInvoice(
            @PathVariable Long id, 
            @RequestParam Long userId,
            @RequestBody Invoice updatedInvoice) {
        try {
            securityUtils.validateUserId(userId); // Bảo mật: kiểm tra userId
            Invoice result = invoiceService.updateInvoice(id, userId, updatedInvoice);
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    // 5. API chuyển đổi Hóa đơn thành Chi tiêu (Cập nhật ngân sách)
    @PostMapping("/{id}/convert")
    public ResponseEntity<?> convertToExpense(@PathVariable Long id, @RequestParam Long userId) {
        try {
            securityUtils.validateUserId(userId); // Bảo mật: kiểm tra userId
            // Thay 'var' bằng 'Expense' để tường minh và tránh lỗi
            Expense expense = invoiceService.convertToExpense(id, userId);
            return ResponseEntity.ok(expense);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }
}