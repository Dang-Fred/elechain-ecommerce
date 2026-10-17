package hcmute.rest;

import hcmute.dto.response.ProductPublicDTO;
import hcmute.dto.response.SanPhamResponse;
import hcmute.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/public/products")
@CrossOrigin(origins = "*") // Tránh lỗi CORS khi Front-end gọi
public class PublicProductController {

    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping
    public ResponseEntity<Page<ProductPublicDTO>> getPublicProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long maDM,
            @RequestParam(required = false) Long maHang,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        Pageable pageable = PageRequest.of(page, size);
        
        Page<ProductPublicDTO> products = sanPhamService.getPublicProducts(
                keyword, minPrice, maxPrice, maDM, maHang, pageable);

        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SanPhamResponse> getPublicProductDetail(@PathVariable Long id) {
        SanPhamResponse response = sanPhamService.getPublicProductDetail(id);
        return ResponseEntity.ok(response);
    }
}