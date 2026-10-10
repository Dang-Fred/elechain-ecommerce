package hcmute.repository;

import hcmute.entity.KhoHang;
import hcmute.entity.KhoHangKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface KhoHangRepository extends JpaRepository<KhoHang, KhoHangKey> {
    
    // Kiểm tra sản phẩm đã từng tồn tại trong kho của chi nhánh cụ thể hay chưa
    Optional<KhoHang> findById_MaSPAndId_MaCN(Long maSP, Long maCN);
    
    // Lấy danh sách kho hàng theo mã SP có số lượng > soLuongMin
    List<KhoHang> findBySanPham_MaSPAndSoLuongTonGreaterThan(Long maSP, Integer soLuongMin);
    
    // Cảnh báo tồn kho theo chi nhánh bằng JPQL
    @Query("SELECT k FROM KhoHang k JOIN FETCH k.sanPham s WHERE k.chiNhanh.maCN = :maCN AND k.soLuongTon < :threshold ORDER BY k.soLuongTon ASC")
    List<KhoHang> findLowStockByChiNhanh(@Param("maCN") Long maCN, @Param("threshold") Integer threshold);

    @Query("SELECT COALESCE(SUM(k.soLuongTon), 0) FROM KhoHang k WHERE k.id.maSP = :maSP")
    Long getTotalTonKhoByMaSP(@Param("maSP") Long maSP);
}