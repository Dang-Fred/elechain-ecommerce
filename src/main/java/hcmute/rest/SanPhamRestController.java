// File: SanPhamRestController.java
package hcmute.rest;

import hcmute.dto.request.SanPhamRequest;
import hcmute.dto.response.SanPhamResponse;
import hcmute.entity.NhanVien;
import hcmute.exception.CustomException;
import hcmute.repository.NhanVienRepository;
import hcmute.service.SanPhamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/san-pham")
public class SanPhamRestController {

    @Autowired
    private SanPhamService sanPhamService;

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

    // UC 28: Thêm mới Sản phẩm (Chỉ Admin)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SanPhamResponse> createSanPham(Principal principal, @Valid @ModelAttribute SanPhamRequest request) {
        requireAdmin(principal);
        SanPhamResponse response = sanPhamService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // UC 29: Cập nhật Sản phẩm (Chỉ Admin)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SanPhamResponse> updateSanPham(
            Principal principal,
            @PathVariable Long id, 
            @Valid @ModelAttribute SanPhamRequest request) {
        requireAdmin(principal);
        SanPhamResponse response = sanPhamService.update(id, request);
        return ResponseEntity.ok(response);
    }
    
 // THÊM DÒNG @PreAuthorize NÀY ĐỂ GHI ĐÈ QUYỀN, CHO PHÉP NV_KHO VÀ QLCN TRUY CẬP
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'QLCN', 'NV_KHO')")
    public ResponseEntity<java.util.List<SanPhamResponse>> getSanPhams(Principal principal) {
        String role = "";
        Long maCN = null;

        // Nếu có đăng nhập, lấy Role và mã Chi Nhánh từ DB thay vì Header
        if (principal != null) {
            String email = principal.getName();
            NhanVien nhanVien = nhanVienRepository.findByEmail(email).orElse(null);
            
            if (nhanVien != null) {
                role = nhanVien.getVaiTro() != null ? nhanVien.getVaiTro() : "";
                if (nhanVien.getChiNhanh() != null) {
                    maCN = nhanVien.getChiNhanh().getMaCN();
                }
            }
        }
        
        return ResponseEntity.ok(sanPhamService.getSanPhams(role, maCN));
    }

    // Xóa mềm (Chỉ Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> softDeleteSanPham(Principal principal, @PathVariable Long id) {
        requireAdmin(principal);
        sanPhamService.softDelete(id);
        return ResponseEntity.ok("Đã ẩn sản phẩm (ngừng kinh doanh) thành công");
    }
    
    // Phục hồi (Chỉ Admin)
    @PutMapping("/{id}/restore")
    public ResponseEntity<String> restoreSanPham(Principal principal, @PathVariable Long id) {
        requireAdmin(principal);
        sanPhamService.restore(id);
        return ResponseEntity.ok("Khôi phục hiển thị sản phẩm thành công");
    }
}