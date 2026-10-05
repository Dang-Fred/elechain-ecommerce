// File: KhachHangRepository.java
package hcmute.repository;

import hcmute.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    boolean existsByEmail(String email);
    
    // Thêm hàm này phục vụ luồng Đăng nhập
    Optional<KhachHang> findByEmail(String email);
}