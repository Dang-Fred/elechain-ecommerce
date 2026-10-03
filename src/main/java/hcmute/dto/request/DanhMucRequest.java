// File: DanhMucRequest.java
package hcmute.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DanhMucRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String tenDM;
    
 // Nhận trực tiếp URL ảnh từ Client (Tạm thời chưa qua Cloudinary xử lý logic upload)
    private String logo; 
}