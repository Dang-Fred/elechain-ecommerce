package hcmute.rest;

import hcmute.dto.response.DashboardSummaryDTO;
import hcmute.dto.response.DoanhThuDTO;
import hcmute.dto.response.SanPhamBanChayDTO;
import hcmute.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardRestController {

    private final DashboardService dashboardService;

    // API 1: Lấy tổng quan (Summary) - Bị thiếu gây ra lỗi 404
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(Principal principal) {
        // Lấy email từ token, service sẽ tự động phân quyền Admin/QLCN
        String email = principal.getName();
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary(email);
        return ResponseEntity.ok(summary);
    }

    // API 2: Xem doanh thu theo tháng 
    @GetMapping("/revenue/monthly")
    public ResponseEntity<List<DoanhThuDTO>> getMonthlyRevenue(
            Principal principal,
            @RequestParam(required = false) Integer year) {
        
        int targetYear = (year != null) ? year : Year.now().getValue();
        String email = principal.getName(); 
        
        List<DoanhThuDTO> result = dashboardService.getMonthlyRevenue(email, targetYear);
        return ResponseEntity.ok(result);
    }

    // API 3: Xem doanh thu theo từng chi nhánh (Dành cho Admin)
    @GetMapping("/revenue/by-branch")
    public ResponseEntity<List<DoanhThuDTO>> getRevenueByBranch(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
            
        List<DoanhThuDTO> result = dashboardService.getRevenueByBranch(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    // API 4: Top 10 Sản phẩm bán chạy nhất
    @GetMapping("/products/top-selling")
    public ResponseEntity<List<SanPhamBanChayDTO>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
            
        List<SanPhamBanChayDTO> result = dashboardService.getTopSellingProducts(limit);
        return ResponseEntity.ok(result);
    }
}