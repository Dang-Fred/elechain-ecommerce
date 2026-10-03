// File: ChiNhanhRepository.java
package hcmute.repository;

import hcmute.entity.ChiNhanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChiNhanhRepository extends JpaRepository<ChiNhanh, Long> {
}