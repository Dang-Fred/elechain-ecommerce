// File: ChiTietPhieuNhapRepository.java
package hcmute.repository;

import hcmute.entity.ChiTietPhieuNhap;
import hcmute.entity.ChiTietPhieuNhapKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChiTietPhieuNhapRepository extends JpaRepository<ChiTietPhieuNhap, ChiTietPhieuNhapKey> {
}