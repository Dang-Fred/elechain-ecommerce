package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterProductResponse {
    private Long maSP;
    private String tenSP;
    private String hinhAnh;
    private BigDecimal giaBan;
    private String trangThai;
}