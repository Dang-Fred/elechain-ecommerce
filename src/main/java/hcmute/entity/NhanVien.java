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

    private String tenNV;
    private String soDienThoai;
    private String matKhau;
    private String vaiTro; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cn")
    private ChiNhanh chiNhanh;
}