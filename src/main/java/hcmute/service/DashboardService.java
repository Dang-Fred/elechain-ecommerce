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

    // 1. Thống kê tổng quan (Summary) - Có Data Isolation
    public DashboardSummaryDTO getDashboardSummary(String email) {
        NhanVien currentStaff = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên từ Token"));

        Long tongDonHang;
        BigDecimal tongDoanhThu;
        Long tongSpDaBan;

        // Phân quyền dữ liệu
        if ("Admin".equalsIgnoreCase(currentStaff.getVaiTro())) {
            tongDonHang = donHangRepository.countTotalOrders();
            tongDoanhThu = donHangRepository.sumTotalRevenue();
            tongSpDaBan = chiTietDonHangRepository.sumTotalProductsSold();
        } else {
            Long maCN = currentStaff.getChiNhanh().getMaCN();
            tongDonHang = donHangRepository.countTotalOrdersByChiNhanh(maCN);
            tongDoanhThu = donHangRepository.sumTotalRevenueByChiNhanh(maCN);
            tongSpDaBan = chiTietDonHangRepository.sumTotalProductsSoldByChiNhanh(maCN);
        }

        return DashboardSummaryDTO.builder()
                .tongSoDonHang(tongDonHang)
                .tongDoanhThu(tongDoanhThu)
                .tongSanPhamDaBan(tongSpDaBan)
                .build();
    }

    // 2. Logic phân quyền & Data Isolation cho Doanh thu theo tháng
    public List<DoanhThuDTO> getMonthlyRevenue(String email, int year) {
        NhanVien currentStaff = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên từ Token"));

        if ("Admin".equalsIgnoreCase(currentStaff.getVaiTro())) {
            // ADMIN: Lấy doanh thu toàn hệ thống
            return donHangRepository.getDoanhThuTheoThangTrongNam(year);
        } else {
            // QLCN: Bắt buộc lấy maCN của chính người đó để filter
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