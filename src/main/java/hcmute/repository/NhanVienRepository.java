package hcmute.repository;

import hcmute.entity.NhanVien;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    Optional<NhanVien> findByEmail(String email);
    
    Optional<NhanVien> findBySoDienThoai(String soDienThoai);
    
    List<NhanVien> findByChiNhanh_MaCN(Long maCN);
}