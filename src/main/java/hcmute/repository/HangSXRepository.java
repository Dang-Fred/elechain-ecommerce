// File: HangSXRepository.java
package hcmute.repository;

import hcmute.entity.HangSX;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HangSXRepository extends JpaRepository<HangSX, Long> {
    // Phục vụ UC 25 (Thêm mới)
    boolean existsByTenHang(String tenHang);

    // Phục vụ UC 26 (Cập nhật) - Bỏ qua chính nó
    boolean existsByTenHangAndMaHangNot(String tenHang, Long maHang);
}