// File: PublicProductController.java
package hcmute.rest;

import hcmute.dto.response.ProductPublicDTO;
import hcmute.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/products")
public class PublicProductController {

    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping
    public ResponseEntity<Page<ProductPublicDTO>> getPublicProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) Long maDM,
            @RequestParam(required = false) Long maHang) {

        // Khởi tạo đối tượng phân trang
        Pageable pageable = PageRequest.of(page, size);
        
        // Gọi Service
        Page<ProductPublicDTO> products = sanPhamService.getPublicProducts(pageable, maDM, maHang);

        return ResponseEntity.ok(products);
    }
}