// File: OrderResponseDTO.java
package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long maDH;
    private LocalDateTime ngayDat;
    private String phuongThucNhan;
    private String trangThai; // Có thể map từ OrderStatus.name() hoặc OrderStatus.getDescription()
    private BigDecimal tongTien;
    
    private String tenKhachHang;
    private String diaChiGiao; // Sẽ map từ bảng DonHang (nếu bạn thêm field này vào DonHang) hoặc KhachHang.DiaChi
    
    private List<OrderDetailResponseDTO> chiTietDonHangs;
}