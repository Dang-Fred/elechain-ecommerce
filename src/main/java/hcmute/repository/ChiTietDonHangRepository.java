// File: ChiTietDonHangRepository.java
package hcmute.repository;

import hcmute.entity.ChiTietDonHang;
import hcmute.entity.ChiTietDonHangKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, ChiTietDonHangKey> {
}