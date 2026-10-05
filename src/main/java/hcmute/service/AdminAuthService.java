package hcmute.service;

import hcmute.dto.request.AdminLoginRequest;
import hcmute.dto.response.AdminLoginResponseDTO;
import hcmute.entity.NhanVien;
import hcmute.repository.NhanVienRepository;
import hcmute.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminAuthService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AdminLoginResponseDTO login(AdminLoginRequest request) throws Exception {
        // 1. Tìm NhanVien qua email
        Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByEmail(request.getEmail());
        
        if (nhanVienOpt.isEmpty()) {
            throw new Exception("UNAUTHORIZED:Tài khoản hoặc mật khẩu không chính xác");
        }

        NhanVien nhanVien = nhanVienOpt.get();

        // 2. Kiểm tra trạng thái
        if (!nhanVien.getTrangThai()) {
            throw new Exception("FORBIDDEN:Tài khoản của bạn đã bị vô hiệu hóa");
        }

        // 3. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getMatKhau(), nhanVien.getMatKhau())) {
            throw new Exception("UNAUTHORIZED:Tài khoản hoặc mật khẩu không chính xác");
        }

        // 4. Sinh Token với Role lấy TRỰC TIẾP TỪ DB (vaiTro)
        String role = nhanVien.getVaiTro();
        if (role == null || role.trim().isEmpty()) {
            throw new Exception("FORBIDDEN:Tài khoản chưa được phân quyền hệ thống");
        }
        
        String token = jwtUtil.generateToken(nhanVien.getEmail(), role);

        // Lấy mã chi nhánh một cách an toàn (tránh NullPointerException nếu NV chưa xếp chi nhánh)
        Long maCN = nhanVien.getChiNhanh() != null ? nhanVien.getChiNhanh().getMaCN() : null;

        return new AdminLoginResponseDTO(token, nhanVien.getTenNV(), role, maCN);
    }
}