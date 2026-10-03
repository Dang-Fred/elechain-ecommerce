// File: ChiTietPhieuNhap.java
package hcmute.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "chi_tiet_phieu_nhap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietPhieuNhap {
    @EmbeddedId
    private ChiTietPhieuNhapKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maPN")
    @JoinColumn(name = "ma_pn")
    private PhieuNhap phieuNhap;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maSP")
    @JoinColumn(name = "ma_sp")
    private SanPham sanPham;

    @Min(value = 1, message = "Số lượng nhập phải lớn hơn 0")
    private Integer soLuongNhap;
}