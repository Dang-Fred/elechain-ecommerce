// File: HangSXRequest.java
package hcmute.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class HangSXRequest {
    @NotBlank(message = "Tên hãng sản xuất không được để trống")
    private String tenHang;
    
 // Nhận file ảnh từ client
    private MultipartFile logoFile;
}