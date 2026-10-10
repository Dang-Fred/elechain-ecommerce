// File: GioHangRepository.java
package hcmute.repository;

import hcmute.entity.GioHang;
import hcmute.entity.GioHangKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, GioHangKey> {
    
    // Tìm giỏ hàng theo mã Khách Hàng và mã Sản Phẩm
    Optional<GioHang> findById_MaKHAndId_MaSP(Long maKH, Long maSP);
    
    // Lấy danh sách giỏ hàng của một Khách Hàng
    List<GioHang> findById_MaKH(Long maKH);
    
    // Xóa sản phẩm khỏi giỏ hàng
    void deleteById_MaKHAndId_MaSP(Long maKH, Long maSP);
}