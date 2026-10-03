// File: KhoHang.java
package hcmute.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "kho_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhoHang {
    @EmbeddedId
    private KhoHangKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maSP")
    @JoinColumn(name = "ma_sp")
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maCN")
    @JoinColumn(name = "ma_cn")
    private ChiNhanh chiNhanh;

    @Min(value = 0, message = "Số lượng tồn không được âm")
    private Integer soLuongTon;
}