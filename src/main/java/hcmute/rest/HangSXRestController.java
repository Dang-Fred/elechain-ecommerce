// File: HangSXRestController.java
package hcmute.rest;

import hcmute.dto.request.HangSXRequest;
import hcmute.dto.response.HangSXResponse;
import hcmute.entity.NhanVien;
import hcmute.exception.CustomException;
import hcmute.repository.NhanVienRepository;
import hcmute.service.HangSXService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hang-sx")
public class HangSXRestController {

    @Autowired
    private HangSXService hangSXService;

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

    @GetMapping
    public ResponseEntity<List<HangSXResponse>> getAllHangSX() {
        return ResponseEntity.ok(hangSXService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HangSXResponse> getHangSXById(@PathVariable Long id) {
        return ResponseEntity.ok(hangSXService.getById(id));
    }

    // Thêm mới (Chỉ Admin)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<HangSXResponse> createHangSX(Principal principal, @Valid @ModelAttribute HangSXRequest request) {
        requireAdmin(principal);
        HangSXResponse response = hangSXService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    // Cập nhật (Chỉ Admin)
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<HangSXResponse> updateHangSX(
            Principal principal,
            @PathVariable Long id, 
            @Valid @ModelAttribute HangSXRequest request) {
        requireAdmin(principal);
        HangSXResponse response = hangSXService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // Xóa (Chỉ Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHangSX(Principal principal, @PathVariable Long id) {
        requireAdmin(principal);
        hangSXService.delete(id);
        return ResponseEntity.ok("Xóa hãng sản xuất thành công");
    }
}