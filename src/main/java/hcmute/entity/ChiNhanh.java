// File: ChiNhanh.java
package hcmute.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "chi_nhanh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiNhanh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maCN;

    private String tenCN;
    private String diaChi;
    private String hotline;

    @OneToMany(mappedBy = "chiNhanh", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<NhanVien> nhanViens;
}