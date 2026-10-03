// File: KhoHangRepository.java
package hcmute.repository;

import hcmute.entity.KhoHang;
import hcmute.entity.KhoHangKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhoHangRepository extends JpaRepository<KhoHang, KhoHangKey> {
}