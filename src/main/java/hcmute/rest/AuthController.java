// File: AuthController.java
package hcmute.rest;

import hcmute.dto.request.RegisterRequestDTO;
import hcmute.repository.KhachHangRepository;
import hcmute.service.EmailService;
import hcmute.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register/send-otp")
    public ResponseEntity<?> sendOtpRegister(@Valid @RequestBody RegisterRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        // 1. Validation: Mật khẩu và Xác nhận mật khẩu
        if (!request.getMatKhau().equals(request.getXacNhanMatKhau())) {
            response.put("success", false);
            response.put("message", "Mật khẩu và xác nhận mật khẩu không khớp!");
            return ResponseEntity.badRequest().body(response);
        }

        // 2. Validation: Kiểm tra Email đã tồn tại chưa
        if (khachHangRepository.existsByEmail(request.getEmail())) {
            response.put("success", false);
            response.put("message", "Email đã tồn tại trong hệ thống. Vui lòng dùng email khác!");
            return ResponseEntity.badRequest().body(response);
        }

        // 3. Tạo OTP, lưu RAM và Gửi Email
        try {
            String otpCode = otpService.generateAndStoreOtp(request.getEmail());
            emailService.sendOtpEmail(request.getEmail(), otpCode);
            
            response.put("success", true);
            response.put("message", "Đã gửi mã OTP đến email " + request.getEmail() + ". Yêu cầu nhập OTP trong vòng 60s.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi gửi email: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}