// File: PhieuNhap.java
package hcmute.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "phieu_nhap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhieuNhap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maPN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cn")
    private ChiNhanh chiNhanh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nv")
    private NhanVien nhanVien;

    @PastOrPresent(message = "Ngày lập không được ở tương lai")
    private LocalDateTime ngayLap;
    
    @OneToMany(mappedBy = "phieuNhap", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChiTietPhieuNhap> chiTietPhieuNhaps;
}