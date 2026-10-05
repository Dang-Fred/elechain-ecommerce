package hcmute.rest;

import hcmute.dto.request.AdminLoginRequest;
import hcmute.dto.response.AdminLoginResponseDTO;
import hcmute.service.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AdminLoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            AdminLoginResponseDTO loginData = adminAuthService.login(request);
            
            response.put("success", true);
            response.put("message", "Đăng nhập nội bộ thành công");
            response.put("data", loginData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            String errorMessage = e.getMessage();
            
            if (errorMessage != null && errorMessage.startsWith("FORBIDDEN:")) {
                response.put("message", errorMessage.split(":")[1]);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            } else if (errorMessage != null && errorMessage.startsWith("UNAUTHORIZED:")) {
                response.put("message", errorMessage.split(":")[1]);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            response.put("message", "Đã xảy ra lỗi hệ thống: " + errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}