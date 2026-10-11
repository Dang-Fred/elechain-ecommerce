//ReviewRestController.java
package hcmute.rest;

import hcmute.dto.request.ReviewRequestDTO;
import hcmute.dto.response.ReviewResponseDTO;
import hcmute.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/reviews")
public class ReviewRestController {

    @Autowired
    private ReviewService reviewService;

    // 1. API Thêm Đánh Giá (Yêu cầu JWT Token)
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            Principal principal, 
            @Valid @RequestBody ReviewRequestDTO request) {
        
        String email = principal.getName();
        ReviewResponseDTO responseDTO = reviewService.taoDanhGia(email, request);
        return ResponseEntity.ok(responseDTO);
    }

    // 2. API Xem danh sách Đánh giá (Public)
    @GetMapping("/product/{maSP}")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviewsByProduct(
            @PathVariable Long maSP,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<ReviewResponseDTO> result = reviewService.getReviewsByProduct(maSP, pageable);
        return ResponseEntity.ok(result);
    }
}