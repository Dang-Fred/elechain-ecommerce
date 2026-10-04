// File: AuthController.java
package hcmute.rest;

import hcmute.dto.request.RegisterRequestDTO;
import hcmute.dto.request.VerifyOtpRequestDTO;
import hcmute.entity.KhachHang;
import hcmute.repository.KhachHangRepository;
import hcmute.service.EmailService;
import hcmute.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    
    @Autowired
    private PasswordEncoder passwordEncoder; // Inject Bean BCrypt

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
        	// [CẬP NHẬT] Băm mật khẩu bằng BCrypt ngay từ bước 1
            String hashedPass = passwordEncoder.encode(request.getMatKhau());
            
            // [CẬP NHẬT] Truyền thông tin vào Cache
            String otpCode = otpService.generateAndStoreOtp(request.getTenKH(), request.getEmail(), hashedPass);
            
            emailService.sendOtpEmail(request.getEmail(), otpCode);
            
            response.put("success", true);
            response.put("message", "Đã gửi mã OTP. Yêu cầu nhập OTP trong vòng 60s.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi gửi email: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    
    @PostMapping("/register/verify")
    public ResponseEntity<?> verifyOtpAndRegister(@Valid @RequestBody VerifyOtpRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        // 1. Kiểm tra trạng thái OTP
        OtpService.OtpStatus status = otpService.validateOtpWithStatus(request.getEmail(), request.getOtp());

        if (status == OtpService.OtpStatus.EXPIRED) {
            response.put("success", false);
            response.put("message", "OTP đã hết hạn hoặc không tồn tại.");
            return ResponseEntity.badRequest().body(response);
        } else if (status == OtpService.OtpStatus.INVALID) {
            response.put("success", false);
            response.put("message", "OTP không chính xác.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Hợp lệ: Lấy Tên, Email, Mật khẩu đã băm từ trong RAM
        OtpService.OtpCacheInfo cacheInfo = otpService.getOtpCacheInfo(request.getEmail());
        
        // 2. Chắc chắn Email chưa bị ai đó đăng ký trong 60s vừa qua
        if (khachHangRepository.existsByEmail(cacheInfo.getEmail())) {
            otpService.clearOtp(request.getEmail());
            response.put("success", false);
            response.put("message", "Email đã tồn tại trong hệ thống. Đăng ký thất bại!");
            return ResponseEntity.badRequest().body(response);
        }

        // 3. Tạo tài khoản khách hàng mới
        // Map sang entity KhachHang
        
        KhachHang khachHang = new KhachHang();
        khachHang.setTenKH(cacheInfo.getTenKH());
        khachHang.setEmail(cacheInfo.getEmail());
        khachHang.setMatKhau(cacheInfo.getMatKhauBam());
        khachHang.setSoDuVi(BigDecimal.ZERO);
        khachHang.setTrangThai(true);
        
        // Lưu xuống Database
        khachHangRepository.save(khachHang);

        // Xóa cache sau khi lưu thành công
        otpService.clearOtp(request.getEmail());
        
        response.put("success", true);
        response.put("message", "Xác thực OTP thành công. Đăng ký tài khoản hoàn tất!");
        return ResponseEntity.ok(response);
            
    }
       
    
}