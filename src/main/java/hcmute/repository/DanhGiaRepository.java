// File: DanhGiaRepository.java
package hcmute.repository;

import hcmute.entity.DanhGia;
import hcmute.entity.DanhGiaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia, DanhGiaKey> {
}