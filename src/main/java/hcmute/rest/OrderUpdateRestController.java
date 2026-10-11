//OrderUpdateRestController.java
package hcmute.rest;

import hcmute.dto.request.OrderStatusRequestDTO;
import hcmute.service.OrderUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderUpdateRestController {

    @Autowired
    private OrderUpdateService orderUpdateService;

    // UC42: Nhân viên/Admin cập nhật trạng thái đơn hàng
    @PutMapping("/api/admin/orders/{maDH}/status")
    public ResponseEntity<?> updateOrderStatus(
            Principal principal,
            @PathVariable Long maDH,
            @RequestBody OrderStatusRequestDTO request) {
        
        String email = principal.getName();
        orderUpdateService.updateOrderStatus(email, maDH, request.getStatus());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cập nhật trạng thái đơn hàng thành công.");
        return ResponseEntity.ok(response);
    }

    // UC18: Khách hàng tự hủy đơn
    @PutMapping("/api/client/orders/{maDH}/cancel")
    public ResponseEntity<?> cancelOrderByClient(Principal principal, @PathVariable Long maDH) {
        String email = principal.getName();
        // false = là khách hàng gọi
        orderUpdateService.cancelOrder(email, maDH, false); 
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hủy đơn và hoàn tiền thành công.");
        return ResponseEntity.ok(response);
    }
}