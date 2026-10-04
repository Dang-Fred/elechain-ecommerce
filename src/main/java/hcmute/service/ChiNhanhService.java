// File: ChiNhanhService.java
package hcmute.service;

import hcmute.dto.request.ChiNhanhRequest;
import hcmute.dto.response.ChiNhanhResponse;
import java.util.*;

public interface ChiNhanhService {
    ChiNhanhResponse create(ChiNhanhRequest request);
    ChiNhanhResponse update(Long id, ChiNhanhRequest request);
    
    List<ChiNhanhResponse> getAllActive();
    ChiNhanhResponse getActiveById(Long id);
    
    void softDelete(Long id);
    void restore(Long id);
}