// File: NhanVien.java
package hcmute.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nhan_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maNV;

    @Column(name = "tennv", columnDefinition = "NVARCHAR(255)")
    private String tenNV;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String soDienThoai;
    private String matKhau;
    private String vaiTro; 
    
    @Column(name = "Avatar", columnDefinition = "NVARCHAR(MAX)")
    private String avatar;
    
    @Column(nullable = false, columnDefinition = "BIT DEFAULT 1")
    private Boolean trangThai = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cn")
    private ChiNhanh chiNhanh;
}