// File: OrderRestController.java
package hcmute.rest;

import hcmute.dto.request.CheckoutRequestDTO;
import hcmute.dto.response.CheckoutResponseDTO;
import hcmute.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/client/order")
public class OrderRestController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(Principal principal, @RequestBody CheckoutRequestDTO request) {
        // Lấy email an toàn từ JWT
        String email = principal.getName();
        
        // Gọi service xử lý Transaction
        CheckoutResponseDTO donHangInfo = orderService.checkout(email, request);
        
        // Trả về JSON thành công
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Thanh toán thành công");
        response.put("donHang", donHangInfo);
        
        return ResponseEntity.ok(response);
    }
}