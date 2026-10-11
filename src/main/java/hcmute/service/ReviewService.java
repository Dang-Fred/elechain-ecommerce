//ReviewService.java
package hcmute.service;

import hcmute.dto.request.ReviewRequestDTO;
import hcmute.dto.response.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponseDTO taoDanhGia(String email, ReviewRequestDTO request);
    
    Page<ReviewResponseDTO> getReviewsByProduct(Long maSP, Pageable pageable);
}