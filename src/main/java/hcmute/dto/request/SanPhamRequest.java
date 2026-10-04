// File: SanPhamRequest.java
package hcmute.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Getter
@Setter
public class SanPhamRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String tenSP;

    @NotNull(message = "Vui lòng chọn Danh mục")
    private Long maDM;

    @NotNull(message = "Vui lòng chọn Hãng sản xuất")
    private Long maHang;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá bán phải lớn hơn 0")
    private BigDecimal giaBan;

    @NotBlank(message = "Thời gian bảo hành không được để trống")
    @Pattern(regexp = "^[0-9]+$", message = "Thời gian bảo hành phải là số hợp lệ và không được âm (VD: 12)")
    private String baoHanh;

    private String cauHinh;

    private String moTa;

    // Nhận file ảnh từ client
    private MultipartFile logoFile;
}