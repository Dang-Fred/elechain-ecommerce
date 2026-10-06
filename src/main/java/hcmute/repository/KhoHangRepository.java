package hcmute.repository;

import hcmute.entity.KhoHang;
import hcmute.entity.KhoHangKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface KhoHangRepository extends JpaRepository<KhoHang, KhoHangKey> {
    
    // Kiểm tra sản phẩm đã từng tồn tại trong kho của chi nhánh cụ thể hay chưa
    Optional<KhoHang> findById_MaSPAndId_MaCN(Long maSP, Long maCN);
}