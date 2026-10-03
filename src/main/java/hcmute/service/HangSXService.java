// File: HangSXService.java
package hcmute.service;

import hcmute.dto.request.HangSXRequest;
import hcmute.dto.response.HangSXResponse;

import java.util.List;

public interface HangSXService {
    List<HangSXResponse> getAll();
    HangSXResponse getById(Long id);
    HangSXResponse create(HangSXRequest request);
    HangSXResponse update(Long id, HangSXRequest request);
    void delete(Long id);
}