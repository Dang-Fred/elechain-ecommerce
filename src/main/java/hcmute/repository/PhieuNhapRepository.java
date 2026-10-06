//File: PhieuNhapResponse.java
package hcmute.repository;

import hcmute.entity.PhieuNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhieuNhapRepository extends JpaRepository<PhieuNhap, Long> {
    
    // Xem lịch sử phiếu nhập theo chi nhánh
    List<PhieuNhap> findByChiNhanh_MaCN(Long maCN);
}