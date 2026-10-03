// File: DanhGia.java
package hcmute.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "danh_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DanhGia {
    @EmbeddedId
    private DanhGiaKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maKH")
    @JoinColumn(name = "ma_kh")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maSP")
    @JoinColumn(name = "ma_sp")
    private SanPham sanPham;

    @Min(value = 1, message = "Số sao tối thiểu là 1")
    @Max(value = 5, message = "Số sao tối đa là 5")
    private Integer soSao;

    @Column(columnDefinition = "TEXT")
    private String binhLuan;
    
    private LocalDateTime ngayDG;
}