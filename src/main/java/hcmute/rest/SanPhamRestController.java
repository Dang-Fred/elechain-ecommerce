// File: SanPhamRestController.java
package hcmute.rest;

import hcmute.dto.request.SanPhamRequest;
import hcmute.dto.response.SanPhamResponse;
import hcmute.service.SanPhamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/san-pham")
public class SanPhamRestController {

    @Autowired
    private SanPhamService sanPhamService;

    // UC 28: Thêm mới Sản phẩm
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SanPhamResponse> createSanPham(@Valid @ModelAttribute SanPhamRequest request) {
        SanPhamResponse response = sanPhamService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // UC 29: Cập nhật Sản phẩm
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SanPhamResponse> updateSanPham(
            @PathVariable Long id, 
            @Valid @ModelAttribute SanPhamRequest request) {
        SanPhamResponse response = sanPhamService.update(id, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<java.util.List<SanPhamResponse>> getSanPhams(
            @RequestHeader(value = "X-Role", required = true) String role,
            @RequestHeader(value = "X-Branch-Id", required = false) Long maCN) {
        
        return ResponseEntity.ok(sanPhamService.getSanPhams(role, maCN));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> softDeleteSanPham(@PathVariable Long id) {
        sanPhamService.softDelete(id);
        return ResponseEntity.ok("Đã ẩn sản phẩm (ngừng kinh doanh) thành công");
    }
    
    
    @PutMapping("/{id}/restore")
    public ResponseEntity<String> restoreSanPham(@PathVariable Long id) {
        sanPhamService.restore(id);
        return ResponseEntity.ok("Khôi phục hiển thị sản phẩm thành công");
    }
    
    
    
    
    
    
    
}