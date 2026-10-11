// File: DonHangRepository.java
package hcmute.repository;

import hcmute.entity.DonHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

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
}