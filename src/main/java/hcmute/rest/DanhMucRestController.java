// File: DanhMucRestController.java
package hcmute.rest;

import hcmute.dto.request.DanhMucRequest;
import hcmute.dto.response.DanhMucResponse;
import hcmute.entity.NhanVien;
import hcmute.exception.CustomException;
import hcmute.repository.NhanVienRepository;
import hcmute.service.DanhMucService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/danh-muc")
public class DanhMucRestController {

    @Autowired
    private DanhMucService danhMucService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    // Hàm private kiểm tra quyền Admin
    private void requireAdmin(Principal principal) {
        if (principal == null) {
            throw new CustomException("Vui lòng đăng nhập", HttpStatus.UNAUTHORIZED);
        }
        String email = principal.getName();
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Không tìm thấy thông tin nhân viên", HttpStatus.NOT_FOUND));

        if (nhanVien.getVaiTro() == null || !nhanVien.getVaiTro().trim().equalsIgnoreCase("Admin")) {
            throw new CustomException("Quyền truy cập bị từ chối. Chỉ Admin mới có thể thực hiện thao tác này.", HttpStatus.FORBIDDEN);
        }
    }

    // Lấy danh sách (Phục vụ hiển thị UC - Public)
    @GetMapping
    public ResponseEntity<List<DanhMucResponse>> getAllDanhMuc() {
        return ResponseEntity.ok(danhMucService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DanhMucResponse> getDanhMucById(@PathVariable Long id) {
        return ResponseEntity.ok(danhMucService.getById(id));
    }

    // UC 22: Thêm mới (Chỉ Admin)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<DanhMucResponse> createDanhMuc(Principal principal, @Valid @ModelAttribute DanhMucRequest request) {
        requireAdmin(principal);
        DanhMucResponse response = danhMucService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // UC 23: Cập nhật (Chỉ Admin)
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<DanhMucResponse> updateDanhMuc(
            Principal principal,
            @PathVariable Long id, 
            @Valid @ModelAttribute DanhMucRequest request) { 
        requireAdmin(principal);
        DanhMucResponse response = danhMucService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // UC 24: Xóa (Chỉ Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDanhMuc(Principal principal, @PathVariable Long id) {
        requireAdmin(principal);
        danhMucService.delete(id);
        return ResponseEntity.ok("Xóa danh mục thành công");
    }
}


