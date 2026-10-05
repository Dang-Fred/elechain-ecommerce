// File: AuthService.java
package hcmute.service;

import hcmute.dto.request.LoginRequestDTO;
import hcmute.dto.response.LoginResponseDTO;
import hcmute.entity.KhachHang;
import hcmute.repository.KhachHangRepository;
import hcmute.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponseDTO login(LoginRequestDTO request) throws Exception {
        // 1. Tìm KhachHang theo email
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByEmail(request.getEmail());
        
        if (khachHangOpt.isEmpty()) {
            throw new Exception("UNAUTHORIZED:Tài khoản hoặc mật khẩu không chính xác");
        }

        KhachHang khachHang = khachHangOpt.get();

        // 2. Kiểm tra trạng thái tài khoản
        if (!khachHang.getTrangThai()) {
            throw new Exception("FORBIDDEN:Tài khoản của bạn đã bị vô hiệu hóa");
        }

        // 3. So sánh mật khẩu bằng BCrypt
        if (!passwordEncoder.matches(request.getPassword(), khachHang.getMatKhau())) {
            throw new Exception("UNAUTHORIZED:Tài khoản hoặc mật khẩu không chính xác");
        }

        // 4. Hợp lệ -> Sinh token (Tạm thời fix cứng role "KHACHHANG")
        String role = "KHACHHANG";
        String token = jwtUtil.generateToken(khachHang.getEmail(), role);

        return new LoginResponseDTO(token, khachHang.getTenKH(), role);
    }
}