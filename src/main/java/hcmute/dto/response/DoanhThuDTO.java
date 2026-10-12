//DoanhThuDTO.java
package hcmute.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class DoanhThuDTO {
    private String mocThoiGian;
    private BigDecimal tongDoanhThu;

    // Constructor 1: Dùng cho truy vấn nhóm theo Tháng (MONTH() trong JPQL trả về Integer)
    public DoanhThuDTO(Integer thang, BigDecimal tongDoanhThu) {
        this.mocThoiGian = "Tháng " + thang;
        this.tongDoanhThu = tongDoanhThu;
    }

    // Constructor 2: Dùng cho truy vấn nhóm theo Chi Nhánh (Tên chi nhánh là String)
    public DoanhThuDTO(String mocThoiGian, BigDecimal tongDoanhThu) {
        this.mocThoiGian = mocThoiGian;
        this.tongDoanhThu = tongDoanhThu;
    }
}
