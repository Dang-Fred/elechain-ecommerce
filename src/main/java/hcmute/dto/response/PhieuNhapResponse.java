package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhieuNhapResponse {
    private Long maPN;
    private LocalDateTime ngayLap;
    private String tenNhanVien;
    private List<ChiTietSanPhamResponse> danhSachSanPham;

    // DTO Inner class hỗ trợ trả về chi tiết sản phẩm trong phiếu nhập
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChiTietSanPhamResponse {
        private Long maSP;
        private String tenSP;
        private Integer soLuongNhap;
    }
}