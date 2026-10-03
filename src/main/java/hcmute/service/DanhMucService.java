// File: DanhMucService.java
package hcmute.service;

import hcmute.dto.request.DanhMucRequest;
import hcmute.dto.response.DanhMucResponse;
import java.util.List;

public interface DanhMucService {
    List<DanhMucResponse> getAll();
    DanhMucResponse getById(Long id);
    DanhMucResponse create(DanhMucRequest request);
    DanhMucResponse update(Long id, DanhMucRequest request);
    void delete(Long id);
}