// File: SanPhamResponse.java
package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamResponse {
    private Long maSP;
    private String tenSP;
    private Long maDM;
    private String tenDM; // Cung cấp thêm tên để Frontend dễ hiển thị
    private Long maHang;
    private String tenHang; // Cung cấp thêm tên
    private BigDecimal giaBan;
    private String baoHanh;
    private String cauHinh;
    private String moTa;
    private String trangThai;
    private String logo;
}