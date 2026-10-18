package hcmute.service;

import hcmute.dto.response.DashboardSummaryDTO;
import hcmute.dto.response.DoanhThuDTO;
import hcmute.dto.response.SanPhamBanChayDTO;
import hcmute.entity.NhanVien;
import hcmute.repository.ChiTietDonHangRepository;
import hcmute.repository.DonHangRepository;
import hcmute.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final NhanVienRepository nhanVienRepository;

    // Hàm Helper kiểm tra Role Admin từ Token hiện tại
    private boolean isAdminUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ADMIN") || 
                               a.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));
    }

    // 1. Thống kê tổng quan (Summary) - Có Data Isolation
    public DashboardSummaryDTO getDashboardSummary(String email) {
        Long tongDonHang;
        BigDecimal tongDoanhThu;
        Long tongSpDaBan;

        // Phân quyền dữ liệu
        if (isAdminUser()) {
            // ADMIN: Lấy số liệu toàn hệ thống, không cần dò bảng Nhân viên
            tongDonHang = donHangRepository.countTotalOrders();
            tongDoanhThu = donHangRepository.sumTotalRevenue();
            tongSpDaBan = chiTietDonHangRepository.sumTotalProductsSold();
        } else {
            // QLCN, NV_BANHANG: Phải có trong bảng NhanVien để lấy mã Chi Nhánh filter
            NhanVien currentStaff = nhanVienRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên từ Token"));

            if (currentStaff.getChiNhanh() == null) {
                throw new RuntimeException("Lỗi dữ liệu: Nhân viên này không thuộc chi nhánh nào.");
            }

            Long maCN = currentStaff.getChiNhanh().getMaCN();
            tongDonHang = donHangRepository.countTotalOrdersByChiNhanh(maCN);
            tongDoanhThu = donHangRepository.sumTotalRevenueByChiNhanh(maCN);
            tongSpDaBan = chiTietDonHangRepository.sumTotalProductsSoldByChiNhanh(maCN);
        }

        return DashboardSummaryDTO.builder()
                .tongSoDonHang(tongDonHang == null ? 0L : tongDonHang)
                .tongDoanhThu(tongDoanhThu == null ? BigDecimal.ZERO : tongDoanhThu)
                .tongSanPhamDaBan(tongSpDaBan == null ? 0L : tongSpDaBan)
                .build();
    }

    // 2. Logic phân quyền & Data Isolation cho Doanh thu theo tháng
    public List<DoanhThuDTO> getMonthlyRevenue(String email, int year) {
        if (isAdminUser()) {
            // ADMIN: Lấy doanh thu toàn hệ thống
            return donHangRepository.getDoanhThuTheoThangTrongNam(year);
        } else {
            // QLCN: Bắt buộc lấy maCN của chính người đó để filter
            NhanVien currentStaff = nhanVienRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên từ Token"));

            if (currentStaff.getChiNhanh() == null) {
                throw new RuntimeException("Lỗi dữ liệu: Nhân viên này không thuộc chi nhánh nào.");
            }

            Long maCN = currentStaff.getChiNhanh().getMaCN();
            return donHangRepository.getDoanhThuTheoThangCuaChiNhanh(year, maCN);
        }
    }

    // 3. Thống kê doanh thu chia theo các chi nhánh (Dành cho Admin)
    public List<DoanhThuDTO> getRevenueByBranch(LocalDateTime startDate, LocalDateTime endDate) {
        return donHangRepository.getDoanhThuChiaTheoChiNhanh(startDate, endDate);
    }

    // 4. Top 10 sản phẩm bán chạy (Sử dụng PageRequest để LIMIT kết quả)
    public List<SanPhamBanChayDTO> getTopSellingProducts(int limit) {
        return chiTietDonHangRepository.getTopSanPhamBanChay(PageRequest.of(0, limit));
    }
}