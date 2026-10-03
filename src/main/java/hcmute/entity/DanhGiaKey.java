// File: DanhGiaKey.java
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
public class DanhGiaKey implements Serializable {
    private Long maKH;
    private Long maSP;
}