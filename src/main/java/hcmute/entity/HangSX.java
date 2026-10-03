// File: HangSX.java
package hcmute.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "hang_sx")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HangSX {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maHang;

    private String tenHang;
    
    @Column(name = "Logo", columnDefinition = "NVARCHAR(MAX)")
    private String logo;

    @OneToMany(mappedBy = "hangSX", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPham> sanPhams;
}