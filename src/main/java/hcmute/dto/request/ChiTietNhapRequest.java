//File: ChiTietNhapRequest.java
package hcmute.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietNhapRequest {
    
    @NotNull(message = "Mã sản phẩm không được để trống")
    private Long maSP;

    @NotNull(message = "Số lượng nhập không được để trống")
    @Min(value = 1, message = "Số lượng nhập phải lớn hơn hoặc bằng 1")
    private Integer soLuongNhap;
}