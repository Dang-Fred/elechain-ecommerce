// File: SanPhamRepository.java
package hcmute.repository;

import hcmute.entity.SanPham;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Long> {
	// Lấy toàn bộ sản phẩm đang hoạt động (Cho ADMIN)
    java.util.List<hcmute.entity.SanPham> findAllByTrangThai(String trangThai);

    // Lấy sản phẩm đang hoạt động thuộc Chi nhánh cụ thể (Cho phe Khu vực)
    @org.springframework.data.jpa.repository.Query("SELECT k.sanPham FROM KhoHang k WHERE k.chiNhanh.maCN = :maCN AND k.sanPham.trangThai = 'true'")
    java.util.List<hcmute.entity.SanPham> findActiveByChiNhanh(@org.springframework.data.repository.query.Param("maCN") Long maCN);

    // Tìm 1 sản phẩm đang hoạt động (Dùng trước khi Xóa mềm)
    java.util.Optional<hcmute.entity.SanPham> findByMaSPAndTrangThai(Long maSP, String trangThai);
    
 // Lấy danh sách sản phẩm public, có phân trang và lọc theo Danh Mục / Hãng SX (Nếu có truyền)
    @org.springframework.data.jpa.repository.Query("SELECT s FROM SanPham s WHERE s.trangThai = 'true' " +
            "AND (:maDM IS NULL OR s.danhMuc.maDM = :maDM) " +
            "AND (:maHang IS NULL OR s.hangSX.maHang = :maHang)")
    org.springframework.data.domain.Page<hcmute.entity.SanPham> findPublicProducts(
            @org.springframework.data.repository.query.Param("maDM") Long maDM,
            @org.springframework.data.repository.query.Param("maHang") Long maHang,
            org.springframework.data.domain.Pageable pageable);
    
    
    @Query("SELECT s FROM SanPham s WHERE s.trangThai = 'true' " +
            "AND (:keyword IS NULL OR LOWER(s.tenSP) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:minPrice IS NULL OR s.giaBan >= :minPrice) " +
            "AND (:maxPrice IS NULL OR s.giaBan <= :maxPrice) " +
            "AND (:maDM IS NULL OR s.danhMuc.maDM = :maDM) " +
            "AND (:maHang IS NULL OR s.hangSX.maHang = :maHang)")
    Page<SanPham> findPublicProducts(
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("maDM") Long maDM,
            @Param("maHang") Long maHang,
            Pageable pageable);
}