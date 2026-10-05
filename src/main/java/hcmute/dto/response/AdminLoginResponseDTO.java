package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminLoginResponseDTO {
    private String token;
    private String tenNV;
    private String vaiTro;
    private Long maCN; // Cần thiết để lọc đơn hàng/doanh thu theo chi nhánh
}