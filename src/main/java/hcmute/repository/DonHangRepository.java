// File: DonHangRepository.java
package hcmute.repository;

import hcmute.entity.DonHang;
import hcmute.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import hcmute.dto.response.DoanhThuDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Long> {
    
    
 // UC17: Khách hàng xem lịch sử đơn của mình (Mới nhất xếp trước)
    @EntityGraph(attributePaths = {"chiTietDonHangs", "chiTietDonHangs.sanPham", "khachHang"})
    Page<DonHang> findByKhachHang_MaKHOrderByNgayDatDesc(Long maKH, Pageable pageable);
    
 // UC41: Nhân viên xem đơn hàng tại chi nhánh của mình quản lý
    @EntityGraph(attributePaths = {"chiTietDonHangs", "chiTietDonHangs.sanPham", "khachHang"})
    Page<DonHang> findByChiNhanh_MaCNOrderByNgayDatDesc(Long maCN, Pageable pageable);

    @EntityGraph(attributePaths = {"chiTietDonHangs", "chiTietDonHangs.sanPham", "khachHang"})
    Page<DonHang> findAll(Pageable pageable);
    
    
    // Query xác thực khách hàng đã mua sản phẩm và đơn hàng đã Hoàn thành
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
           "FROM DonHang d JOIN d.chiTietDonHangs ct " +
           "WHERE d.khachHang.maKH = :maKH " +
           "AND ct.sanPham.maSP = :maSP " +
           "AND d.trangThai = :trangThai")
    boolean existsCompletedOrderByKhachHangAndSanPham(
            @Param("maKH") Long maKH, 
            @Param("maSP") Long maSP, 
            @Param("trangThai") OrderStatus trangThai);
    
    
 // Hàm 1: Thống kê doanh thu theo từng tháng trong năm (Admin - UC 36)
    // Tự động map vào Constructor(Integer, BigDecimal) của DoanhThuDTO
    @Query("SELECT new hcmute.dto.response.DoanhThuDTO(MONTH(d.ngayDat), SUM(d.tongTien)) " +
           "FROM DonHang d " +
           "WHERE d.trangThai = hcmute.enums.OrderStatus.COMPLETED " +
           "AND YEAR(d.ngayDat) = :year " +
           "GROUP BY MONTH(d.ngayDat) " +
           "ORDER BY MONTH(d.ngayDat) ASC")
    List<DoanhThuDTO> getDoanhThuTheoThangTrongNam(@Param("year") int year);

    // Hàm 2: Thống kê doanh thu theo từng tháng trong năm của một chi nhánh cụ thể (UC 38)
    @Query("SELECT new hcmute.dto.response.DoanhThuDTO(MONTH(d.ngayDat), SUM(d.tongTien)) " +
           "FROM DonHang d " +
           "WHERE d.trangThai = hcmute.enums.OrderStatus.COMPLETED " +
           "AND d.chiNhanh.maCN = :maCN " +
           "AND YEAR(d.ngayDat) = :year " +
           "GROUP BY MONTH(d.ngayDat) " +
           "ORDER BY MONTH(d.ngayDat) ASC")
    List<DoanhThuDTO> getDoanhThuTheoThangCuaChiNhanh(@Param("year") int year, @Param("maCN") Long maCN);

    // Hàm 3: Thống kê tổng doanh thu toàn chuỗi chia theo từng chi nhánh trong 1 khoảng thời gian (UC 37)
    // Tự động map vào Constructor(String, BigDecimal) của DoanhThuDTO
    @Query("SELECT new hcmute.dto.response.DoanhThuDTO(d.chiNhanh.tenCN, SUM(d.tongTien)) " +
           "FROM DonHang d " +
           "WHERE d.trangThai = hcmute.enums.OrderStatus.COMPLETED " +
           "AND d.ngayDat >= :startDate AND d.ngayDat <= :endDate " +
           "GROUP BY d.chiNhanh.maCN, d.chiNhanh.tenCN " +
           "ORDER BY SUM(d.tongTien) DESC")
    List<DoanhThuDTO> getDoanhThuChiaTheoChiNhanh(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
 // --- CÁC HÀM TÍNH TỔNG QUAN (ADMIN TOÀN HỆ THỐNG) ---
    @Query("SELECT COUNT(d.maDH) FROM DonHang d WHERE d.trangThai = hcmute.enums.OrderStatus.COMPLETED")
    Long countTotalOrders();

    @Query("SELECT COALESCE(SUM(d.tongTien), 0) FROM DonHang d WHERE d.trangThai = hcmute.enums.OrderStatus.COMPLETED")
    BigDecimal sumTotalRevenue();

    // --- CÁC HÀM TÍNH TỔNG QUAN (CHO QUẢN LÝ CHI NHÁNH) ---
    @Query("SELECT COUNT(d.maDH) FROM DonHang d WHERE d.trangThai = hcmute.enums.OrderStatus.COMPLETED AND d.chiNhanh.maCN = :maCN")
    Long countTotalOrdersByChiNhanh(@Param("maCN") Long maCN);

    @Query("SELECT COALESCE(SUM(d.tongTien), 0) FROM DonHang d WHERE d.trangThai = hcmute.enums.OrderStatus.COMPLETED AND d.chiNhanh.maCN = :maCN")
    BigDecimal sumTotalRevenueByChiNhanh(@Param("maCN") Long maCN);
    
 // Truy vấn đơn hàng theo mã đơn và email khách hàng
    // Sử dụng @EntityGraph để xử lý dứt điểm Lazy Loading cho chiTietDonHangs và sanPham (tránh N+1 query)
    @EntityGraph(attributePaths = {"chiTietDonHangs", "chiTietDonHangs.sanPham", "khachHang"})
    Optional<DonHang> findByMaDHAndKhachHang_Email(Long maDH, String email);
    
    
    
 // Đếm số lượng đơn hàng theo trạng thái (Toàn hệ thống - Dành cho Admin)
    Long countByTrangThai(OrderStatus trangThai);

    // Đếm tổng số lượng đơn hàng của 1 Chi Nhánh
    Long countByChiNhanh_MaCN(Long maCN);

    // Đếm số lượng đơn hàng theo trạng thái của 1 Chi Nhánh (Dành cho QLCN/NV)
    Long countByTrangThaiAndChiNhanh_MaCN(OrderStatus trangThai, Long maCN);
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}