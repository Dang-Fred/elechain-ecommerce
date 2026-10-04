// File: SanPhamRepository.java
package hcmute.repository;

import hcmute.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
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
}