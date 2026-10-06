package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchStockResponse {
    private Long maCN;
    private String tenCN;
    private String diaChi;
    private String hotline;
    private Integer soLuongTon;
}