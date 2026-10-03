// File: ChiTietDonHang.java
package hcmute.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "chi_tiet_don_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietDonHang {
    @EmbeddedId
    private ChiTietDonHangKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maDH")
    @JoinColumn(name = "ma_dh")
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maSP")
    @JoinColumn(name = "ma_sp")
    private SanPham sanPham;

    private Integer soLuong;

    @Min(value = 0, message = "Đơn giá không được âm")
    private BigDecimal donGia;
}