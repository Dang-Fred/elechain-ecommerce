//File: PhieuNhapRequest.java
package hcmute.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhieuNhapRequest {
    
    @NotEmpty(message = "Danh sách sản phẩm nhập không được rỗng")
    @Valid
    private List<ChiTietNhapRequest> danhSachSanPham;
}