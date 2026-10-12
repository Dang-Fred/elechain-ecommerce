// File: ChiTietDonHangRepository.java
package hcmute.repository;

import hcmute.entity.ChiTietDonHang;
import hcmute.entity.ChiTietDonHangKey; 
import hcmute.dto.response.SanPhamBanChayDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Object> {
    List<ChiTietDonHang> findByDonHang_MaDH(Long maDH);
    
 // Hàm lấy Top 10 Sản phẩm bán chạy nhất toàn hệ thống (UC 39)
    // Lưu ý: Lọc d.trangThai = COMPLETED để đảm bảo chỉ tính sản phẩm đã bán thành công
    // Tham số Pageable dùng để lấy giới hạn Top 10 ở tầng Service (VD: PageRequest.of(0, 10))
    @Query("SELECT new hcmute.dto.response.SanPhamBanChayDTO(sp.maSP, sp.tenSP, sp.logo, SUM(c.soLuong)) " +
            "FROM ChiTietDonHang c " +
            "JOIN c.donHang d " +
            "JOIN c.sanPham sp " +
            "WHERE d.trangThai = hcmute.enums.OrderStatus.COMPLETED " +
            "GROUP BY sp.maSP, sp.tenSP, sp.logo " +
            "ORDER BY SUM(c.soLuong) DESC")
     List<SanPhamBanChayDTO> getTopSanPhamBanChay(Pageable pageable);

     // Tính tổng số lượng sản phẩm bán ra (Dùng hàm COALESCE để trả về 0 nếu chưa có data)
     @Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietDonHang c JOIN c.donHang d " +
            "WHERE d.trangThai = hcmute.enums.OrderStatus.COMPLETED")
     Long sumTotalProductsSold();

     @Query("SELECT COALESCE(SUM(c.soLuong), 0) FROM ChiTietDonHang c JOIN c.donHang d " +
            "WHERE d.trangThai = hcmute.enums.OrderStatus.COMPLETED AND d.chiNhanh.maCN = :maCN")
     Long sumTotalProductsSoldByChiNhanh(@Param("maCN") Long maCN);
}