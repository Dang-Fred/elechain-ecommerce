//File: InventoryController.java
package hcmute.rest;

import hcmute.dto.request.PhieuNhapRequest;
import hcmute.dto.response.BranchStockResponse;
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
    
    @GetMapping("/check/{maSP}")
    @PreAuthorize("permitAll()") // Ghi đè @PreAuthorize cấp class để mở Public API
    public ResponseEntity<?> checkStock(@PathVariable Long maSP) {
        try {
            List<BranchStockResponse> branches = inventoryService.checkStockByProduct(maSP);
            
            if (branches.isEmpty()) {
                return ResponseEntity.ok("Sản phẩm hiện đang tạm hết hàng tại tất cả các chi nhánh.");
            }
            return ResponseEntity.ok(branches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hệ thống khi tra cứu tồn kho: " + e.getMessage());
        }
    }
    
    @GetMapping("/alert")
    @PreAuthorize("hasAnyAuthority('NV_KHO', 'ADMIN', 'QLCN')") // Tường minh quyền truy cập (kế thừa từ class)
    public ResponseEntity<?> getLowStockAlert() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<LowStockAlertResponse> alerts = inventoryService.getLowStockAlert(email);
            
            if (alerts.isEmpty()) {
                return ResponseEntity.ok("Không có sản phẩm nào sắp hết hàng (Tất cả đều >= 5).");
            }
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hệ thống khi tải cảnh báo: " + e.getMessage());
        }
    }
    
    
    
    
}