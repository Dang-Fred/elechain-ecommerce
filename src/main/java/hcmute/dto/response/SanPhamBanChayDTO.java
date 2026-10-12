//SanPhamBanChayDTO.java
package hcmute.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamBanChayDTO {
    private Long maSP;
    private String tenSP;
    private String hinhAnh; // Ánh xạ với thuộc tính logo của Entity SanPham
    private Long tongSoLuongBan;
}