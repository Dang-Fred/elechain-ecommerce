// File: KhachHang.java
package hcmute.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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

    // RBTV14 (Cập nhật): Email bắt buộc và duy nhất, dùng để đăng nhập
    @Email(message = "Email không đúng định dạng")
    @Column(unique = true, nullable = false)
    private String email;

    // Cập nhật: Nullable (Khách hàng sẽ bổ sung sau)
    private String soDienThoai;

    private String matKhau;
    
    // Cập nhật: Nullable (Khách hàng sẽ bổ sung sau)
    private String diaChi;
    
    @Column(name = "Avatar", columnDefinition = "NVARCHAR(MAX)")
    private String avatar;

    @Min(value = 0, message = "Số dư ví không được âm")
    private BigDecimal soDuVi;
    
    // Cập nhật: Dùng để khóa (ban) hoặc vô hiệu hóa tài khoản (Soft delete)
    @Column(nullable = false, columnDefinition = "BIT DEFAULT 1")
    private Boolean trangThai = true;

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DonHang> donHangs;
}