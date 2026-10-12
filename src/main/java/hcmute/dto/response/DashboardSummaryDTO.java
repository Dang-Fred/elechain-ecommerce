//DashboardSummaryDTO.java
package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {
    private Long tongSoDonHang;
    private BigDecimal tongDoanhThu;
    private Long tongSanPhamDaBan;
}