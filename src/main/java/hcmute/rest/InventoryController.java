// File: InventoryController.java
package hcmute.rest;

import hcmute.dto.request.PhieuNhapRequest;
import hcmute.dto.response.BranchStockResponse;
import hcmute.dto.response.InventoryItemResponse; // Bổ sung import DTO mới
import hcmute.dto.response.LowStockAlertResponse;
import hcmute.dto.response.PhieuNhapResponse;
import hcmute.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
// ĐÃ XÓA: @PreAuthorize cấp class để không gây xung đột với API Public
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // 1. API: Lấy toàn bộ tồn kho hiện tại của chi nhánh (THÊM MỚI)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('NV_KHO', 'ADMIN', 'QLCN')")
    public ResponseEntity<?> getAllStock() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<InventoryItemResponse> stockList = inventoryService.getAllStock(email);
            return ResponseEntity.ok(stockList); // Luôn trả về List
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // 2. API: Tạo phiếu nhập kho
    @PostMapping("/receipt")
    @PreAuthorize("hasAnyAuthority('NV_KHO', 'ADMIN', 'QLCN')") // Dời xuống cấp Method
    public ResponseEntity<?> createReceipt(@Valid @RequestBody PhieuNhapRequest request) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            inventoryService.createPhieuNhap(email, request);
            return ResponseEntity.ok("Tạo phiếu nhập thành công. Đã cập nhật số lượng tồn kho!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. API: Xem lịch sử phiếu nhập
    @GetMapping("/history")
    @PreAuthorize("hasAnyAuthority('NV_KHO', 'ADMIN', 'QLCN')") // Dời xuống cấp Method
    public ResponseEntity<?> getHistory() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<PhieuNhapResponse> history = inventoryService.getLichSuNhapKho(email);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 4. API: Tra cứu tồn kho (Public API)
    @GetMapping("/check/{maSP}")
    // KHÔNG CẦN @PreAuthorize ở đây vì SecurityConfig đã cấu hình permitAll() cho /api/inventory/check/**
    public ResponseEntity<?> checkStock(@PathVariable Long maSP) {
        try {
            List<BranchStockResponse> branches = inventoryService.checkStockByProduct(maSP);
            // ĐÃ XÓA ĐOẠN IF EMPTY TRẢ VỀ STRING. BÂY GIỜ LUÔN TRẢ VỀ ARRAY.
            return ResponseEntity.ok(branches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hệ thống khi tra cứu tồn kho: " + e.getMessage());
        }
    }
    
    // 5. API: Cảnh báo tồn kho thấp
    @GetMapping("/alert")
    @PreAuthorize("hasAnyAuthority('NV_KHO', 'ADMIN', 'QLCN')") // Dời xuống cấp Method
    public ResponseEntity<?> getLowStockAlert() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<LowStockAlertResponse> alerts = inventoryService.getLowStockAlert(email);
            // ĐÃ XÓA ĐOẠN IF EMPTY TRẢ VỀ STRING. BÂY GIỜ LUÔN TRẢ VỀ ARRAY.
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hệ thống khi tải cảnh báo: " + e.getMessage());
        }
    }
}