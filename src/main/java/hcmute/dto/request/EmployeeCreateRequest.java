package hcmute.dto.request;

import lombok.Data;

@Data
public class EmployeeCreateRequest {
    private String tenNV;
    private String email;
    private String soDienThoai;
    private String matKhau;
    private String vaiTro; // Ví dụ: "ADMIN", "NV_BANHANG"
    private Long maCN;     // Có thể null nếu là ADMIN
}