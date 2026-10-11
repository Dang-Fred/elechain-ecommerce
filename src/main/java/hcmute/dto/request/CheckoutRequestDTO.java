// File: CheckoutRequestDTO.java
package hcmute.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {
    
    // Giao tận nơi hoặc Nhận tại cửa hàng
    private String phuongThucNhan; 
    
    // Mã chi nhánh (Chi nhánh xuất hàng đi giao hoặc chi nhánh khách tới lấy)
    private Long maCN; 
    
    // Địa chỉ giao hàng (Có thể null nếu phuongThucNhan là 'Nhận tại cửa hàng')
    private String diaChiGiao; 
}