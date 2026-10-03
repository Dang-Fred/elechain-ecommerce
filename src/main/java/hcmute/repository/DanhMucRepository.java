// File: DanhMucRepository.java
package hcmute.repository;

import hcmute.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {
    // Kiểm tra trùng tên khi Thêm mới (UC 22)
    boolean existsByTenDM(String tenDM);

    // Kiểm tra trùng tên khi Cập nhật (UC 23) - bỏ qua chính danh mục đang sửa
    boolean existsByTenDMAndMaDMNot(String tenDM, Long maDM);
}