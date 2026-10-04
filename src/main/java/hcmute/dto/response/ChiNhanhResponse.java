// File: ChiNhanhResponse.java
package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiNhanhResponse {
    private Long maCN;
    private String tenCN;
    private String diaChi;
    private String hotline;
}