// File: HangSXRepository.java
package hcmute.repository;

import hcmute.entity.HangSX;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HangSXRepository extends JpaRepository<HangSX, Long> {
}