// File: KhoHangKey.java
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

public class KhoHangKey implements Serializable {
    private Long maSP;
    private Long maCN;
}