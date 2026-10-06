package hcmute.repository;

import hcmute.entity.KhoHang;
import hcmute.entity.KhoHangKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface KhoHangRepository extends JpaRepository<KhoHang, KhoHangKey> {
    
    // Kiểm tra sản phẩm đã từng tồn tại trong kho của chi nhánh cụ thể hay chưa
    Optional<KhoHang> findById_MaSPAndId_MaCN(Long maSP, Long maCN);
    
    // Lấy danh sách kho hàng theo mã SP có số lượng > soLuongMin
    List<KhoHang> findBySanPham_MaSPAndSoLuongTonGreaterThan(Long maSP, Integer soLuongMin);

}