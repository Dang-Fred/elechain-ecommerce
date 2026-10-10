// File: CartItemResponseDTO.java
package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {
    private Long maSP;          // Mã sản phẩm
    private String tenSP;       // Tên sản phẩm
    private String logo;        // Hình ảnh sản phẩm (lấy từ trường logo)
    private BigDecimal giaBan;  // Giá bán
    private Integer soLuong;    // Số lượng khách đã thêm vào giỏ
    private Long tongTonKho;    // Tổng số lượng tồn trên tất cả chi nhánh
}