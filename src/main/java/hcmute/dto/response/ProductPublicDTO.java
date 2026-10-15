// File: ProductPublicDTO.java
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
public class ProductPublicDTO {
    private Long maSP;
    private String tenSP;
    private String logo;
    private BigDecimal giaBan;
}