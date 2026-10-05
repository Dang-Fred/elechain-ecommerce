// File: TopupRequest.java
package hcmute.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopupRequest {
    private BigDecimal soTien;
}