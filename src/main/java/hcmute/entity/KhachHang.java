// File: KhachHang.java
package hcmute.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "khach_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor


public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maKH;

    private String tenKH;

    @Column(unique = true, nullable = false)
    private String soDienThoai;

    private String matKhau;
    private String diaChi;
    
    @Column(name = "Avatar", columnDefinition = "NVARCHAR(MAX)")
    private String avatar;

    @Min(value = 0, message = "Số dư ví không được âm")
    private BigDecimal soDuVi;

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DonHang> donHangs;
}