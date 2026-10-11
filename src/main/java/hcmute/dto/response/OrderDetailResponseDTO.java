// File: OrderDetailResponseDTO.java
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
public class OrderDetailResponseDTO {
    private Long maSP;
    private String tenSP;
    private Integer soLuong;
    private BigDecimal donGia;
    private String hinhAnh;
}