// File: SanPhamService.java
package hcmute.service;

import hcmute.dto.request.SanPhamRequest;
import hcmute.dto.response.SanPhamResponse;
import hcmute.dto.response.ProductPublicDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

public interface SanPhamService {
    SanPhamResponse create(SanPhamRequest request);
    SanPhamResponse update(Long id, SanPhamRequest request);
    
    List<SanPhamResponse> getSanPhams(String role, Long maCN);
    void softDelete(Long id);
    void restore(Long id);
    
    Page<ProductPublicDTO> getPublicProducts(Pageable pageable, Long maDM, Long maHang);
    
    Page<ProductPublicDTO> getPublicProducts(
            String keyword, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            Long maDM, 
            Long maHang, 
            Pageable pageable);
    
    SanPhamResponse getPublicProductDetail(Long maSP);
}