// File: ProfileController.java
package hcmute.rest;

import hcmute.dto.request.ChangePasswordRequest;
import hcmute.dto.request.UpdateProfileRequest;
import hcmute.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            profileService.updateProfile(request);
            response.put("success", true);
            response.put("message", "Cập nhật hồ sơ thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, response);
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            profileService.changePassword(request);
            response.put("success", true);
            response.put("message", "Đổi mật khẩu thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, response);
        }
    }

    // Hàm hỗ trợ parse exception để trả về HTTP Status chuẩn xác
    private ResponseEntity<?> handleException(Exception e, Map<String, Object> response) {
        response.put("success", false);
        String errorMessage = e.getMessage();

        if (errorMessage.startsWith("BAD_REQUEST:")) {
            response.put("message", errorMessage.split(":")[1]);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else if (errorMessage.startsWith("UNAUTHORIZED:")) {
            response.put("message", errorMessage.split(":")[1]);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        response.put("message", errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}