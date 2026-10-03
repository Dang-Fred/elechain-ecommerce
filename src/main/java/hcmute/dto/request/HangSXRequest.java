// File: HangSXRequest.java
package hcmute.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HangSXRequest {
    @NotBlank(message = "Tên hãng sản xuất không được để trống")
    private String tenHang;
    
    // Nhận trực tiếp URL ảnh từ Client (Tạm thời chưa qua Cloudinary xử lý logic upload)
    private String logo; 
}