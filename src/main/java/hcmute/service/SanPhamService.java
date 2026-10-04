// File: SanPhamService.java
package hcmute.service;

import hcmute.dto.request.SanPhamRequest;
import hcmute.dto.response.SanPhamResponse;
import java.util.*;

public interface SanPhamService {
    SanPhamResponse create(SanPhamRequest request);
    SanPhamResponse update(Long id, SanPhamRequest request);
    
    List<hcmute.dto.response.SanPhamResponse> getSanPhams(String role, Long maCN);
    void softDelete(Long id);
    void restore(Long id);
}