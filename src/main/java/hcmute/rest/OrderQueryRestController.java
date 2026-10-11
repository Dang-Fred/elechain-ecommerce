// File: OrderQueryRestController.java
package hcmute.rest;

import hcmute.dto.response.OrderResponseDTO;
import hcmute.service.OrderQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.security.Principal;
import java.util.List;

@RestController
public class OrderQueryRestController {

    @Autowired
    private OrderQueryService orderQueryService;

    // UC17: Khách hàng lấy danh sách đơn của mình
    @GetMapping("/api/client/orders/my-orders")
    public ResponseEntity<Page<OrderResponseDTO>> getMyOrders(
            Principal principal, 
            @PageableDefault(size = 10) Pageable pageable) {
        
        String email = principal.getName();
        Page<OrderResponseDTO> result = orderQueryService.getMyOrders(email, pageable);
        return ResponseEntity.ok(result);
    }

    // UC41: Nhân viên/Admin lấy danh sách đơn của chi nhánh mình
    @GetMapping("/api/admin/orders")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByBranch(
            Principal principal, 
            @PageableDefault(size = 20) Pageable pageable) {
        
        String email = principal.getName();
        Page<OrderResponseDTO> result = orderQueryService.getOrdersByBranch(email, pageable);
        return ResponseEntity.ok(result);
    }
}