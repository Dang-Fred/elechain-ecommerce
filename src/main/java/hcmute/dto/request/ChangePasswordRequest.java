// File: ChangePasswordRequest.java
package hcmute.dto.request;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String matKhauCu;
    private String matKhauMoi;
    private String xacNhanMatKhauMoi;
}