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

public class ChiTietDonHangKey implements Serializable {
    private Long maDH;
    private Long maSP;
}