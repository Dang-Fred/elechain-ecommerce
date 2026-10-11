//OrderUpdateService.java
package hcmute.service;

import hcmute.enums.OrderStatus;

public interface OrderUpdateService {
    // Cho Nhân viên (UC42)
    void updateOrderStatus(String email, Long maDH, OrderStatus newStatus);
    
    // Hàm Hủy đơn dùng chung cho cả Khách hàng và Nhân viên (UC18)
    void cancelOrder(String email, Long maDH, boolean isStaff);
}