// File: ChiNhanhRepository.java
package hcmute.repository;

import hcmute.entity.ChiNhanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChiNhanhRepository extends JpaRepository<ChiNhanh, Long> {
    // Phục vụ UC 31 (Thêm mới)
    boolean existsByTenCN(String tenCN);
    boolean existsByHotline(String hotline);

    // Phục vụ UC 32 (Cập nhật) - Bỏ qua chính chi nhánh đang sửa
    boolean existsByTenCNAndMaCNNot(String tenCN, Long maCN);
    boolean existsByHotlineAndMaCNNot(String hotline, Long maCN);
    
    
    // Lấy danh sách các chi nhánh đang hoạt động
    java.util.List<ChiNhanh> findAllByTrangThaiTrue();

    // Tìm chi nhánh theo ID và đang hoạt động
    java.util.Optional<ChiNhanh> findByMaCNAndTrangThaiTrue(Long maCN);
}