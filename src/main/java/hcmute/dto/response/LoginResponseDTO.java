// File: LoginResponseDTO.java
package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String hoTen;
    private String role;
}