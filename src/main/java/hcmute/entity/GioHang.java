// File: GioHang.java
package hcmute.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "gio_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GioHang {
    @EmbeddedId
    private GioHangKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maKH")
    @JoinColumn(name = "ma_kh")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maSP")
    @JoinColumn(name = "ma_sp")
    private SanPham sanPham;

    @Min(value = 1, message = "Số lượng trong giỏ phải lớn hơn 0")
    private Integer soLuong;
}