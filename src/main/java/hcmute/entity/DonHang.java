// File: DonHang.java
package hcmute.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "don_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maDH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kh")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cn")
    private ChiNhanh chiNhanh;

    private LocalDateTime ngayDat;
    private String phuongThucNhan;
    private String trangThai;
    
    private BigDecimal tongTien;
    
    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChiTietDonHang> chiTietDonHangs;
}