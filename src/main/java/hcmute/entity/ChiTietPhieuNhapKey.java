// File: ChiTietPhieuNhapKey.java
package hcmute.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChiTietPhieuNhapKey implements Serializable {
    private Long maPN;
    private Long maSP;
}