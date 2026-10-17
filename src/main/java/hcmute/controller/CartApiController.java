package hcmute.controller;

import hcmute.dto.request.CartRequestDTO; // Đảm bảo import đúng đường dẫn DTO của bạn
import hcmute.service.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    private GioHangService gioHangService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartRequestDTO request, Principal principal) {
        // 1. Kiểm tra đăng nhập
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Vui lòng đăng nhập!"));
        }

        try {
            // 2. Lấy email từ token
            String email = principal.getName();

            // 3. Truyền thẳng DTO vào Service theo đúng cấu trúc của bạn
            gioHangService.themVaoGioHang(email, request);

            // 4. Trả về thành công
            return ResponseEntity.ok().body(Map.of("success", true, "message", "Đã thêm vào giỏ hàng"));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}