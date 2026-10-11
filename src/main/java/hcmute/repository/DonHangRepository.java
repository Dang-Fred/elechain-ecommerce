// File: DonHangRepository.java
package hcmute.repository;

import hcmute.entity.DonHang;
import hcmute.enums.OrderStatus;
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
    
    
    
    
    
    
}