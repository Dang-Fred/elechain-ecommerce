// File: SanPham.java
package hcmute.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "san_pham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maSP;

    @Column(name = "tensp", columnDefinition = "NVARCHAR(255)")
    private String tenSP;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_dm")
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hang")
    private HangSX hangSX;

    private BigDecimal giaBan;
    private String baoHanh;
    
    @Column(columnDefinition = "TEXT")
    private String cauHinh;
    
    @Column(columnDefinition = "TEXT")
    private String moTa;
    
    private String trangThai;
    
    @Column(name = "Logo", columnDefinition = "NVARCHAR(MAX)")
    private String logo;
}