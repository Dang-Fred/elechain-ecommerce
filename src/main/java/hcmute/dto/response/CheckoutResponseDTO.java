// File: CheckoutResponseDTO.java
package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime; 

@Getter
@Setter
@AllArgsConstructor
public class CheckoutResponseDTO {
    private Long maDH;
    private LocalDateTime ngayDat; 
    private String phuongThucNhan;
    private String trangThai;
    private BigDecimal tongTien;
}