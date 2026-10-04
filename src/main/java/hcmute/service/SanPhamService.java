// File: SanPhamService.java
package hcmute.service;

import hcmute.dto.request.SanPhamRequest;
import hcmute.dto.response.SanPhamResponse;

public interface SanPhamService {
    SanPhamResponse create(SanPhamRequest request);
    SanPhamResponse update(Long id, SanPhamRequest request);
}