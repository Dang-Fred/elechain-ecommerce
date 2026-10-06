//File: InventoryController.java
package hcmute.rest;

import hcmute.dto.request.PhieuNhapRequest;
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
@PreAuthorize("hasAnyAuthority('NV_KHO', 'ADMIN', 'QLCN')")// RBTV17: Chỉ Nhân viên kho hoặc Admin, QLCN mới được phép
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/receipt")
    public ResponseEntity<?> createReceipt(@Valid @RequestBody PhieuNhapRequest request) {
        try {
            // Trích xuất email từ JWT Token thông qua Security Context
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            inventoryService.createPhieuNhap(email, request);
            
            return ResponseEntity.ok("Tạo phiếu nhập thành công. Đã cập nhật số lượng tồn kho!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        try {
            // Trích xuất email từ JWT Token để lấy lịch sử kho của đúng chi nhánh nhân viên đó đang làm việc
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<PhieuNhapResponse> history = inventoryService.getLichSuNhapKho(email);
            
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}