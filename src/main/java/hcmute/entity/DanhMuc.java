// File: DanhMuc.java
package hcmute.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "danh_muc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DanhMuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maDM;

    private String tenDM;

    @OneToMany(mappedBy = "danhMuc", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPham> sanPhams;
}