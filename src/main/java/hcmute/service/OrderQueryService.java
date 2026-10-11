// File: OrderQueryService.java
package hcmute.service;

import hcmute.dto.response.OrderResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderQueryService {
    // Cho Khách Hàng (UC17)
	Page<OrderResponseDTO> getMyOrders(String email, Pageable pageable);
    
    // Cho Nhân Viên (UC41)
	Page<OrderResponseDTO> getOrdersByBranch(String email, Pageable pageable);
}