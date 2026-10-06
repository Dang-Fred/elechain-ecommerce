package hcmute.dto.response;

import lombok.Data;

@Data
public class EmployeeResponse {
    private Long maNV;
    private String tenNV;
    private String email;
    private String soDienThoai;
    private String vaiTro;
    private Boolean trangThai;
    private Long maCN; // Trả về ID Chi nhánh để FE tiện hiển thị/lọc
}