// File: ProfileService.java
package hcmute.service;

import hcmute.dto.request.ChangePasswordRequest;
import hcmute.dto.request.UpdateProfileRequest;
import hcmute.entity.KhachHang;
import hcmute.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Hàm tiện ích nội bộ: Lấy thông tin user đang gọi API
    private KhachHang getCurrentUser() throws Exception {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("UNAUTHORIZED:Không tìm thấy thông tin tài khoản"));
    }

    public void updateProfile(UpdateProfileRequest request) throws Exception {
        KhachHang khachHang = getCurrentUser();

        // Kiểm tra logic số điện thoại nếu client có gửi lên
        if (request.getSoDienThoai() != null && !request.getSoDienThoai().trim().isEmpty()) {
            Optional<KhachHang> existingKhachHang = khachHangRepository.findBySoDienThoai(request.getSoDienThoai());
            
            // Nếu có người dùng số này và ID không phải của user hiện tại -> Báo trùng
            if (existingKhachHang.isPresent() && !existingKhachHang.get().getMaKH().equals(khachHang.getMaKH())) {
                throw new Exception("BAD_REQUEST:Số điện thoại đã được sử dụng bởi tài khoản khác");
            }
            khachHang.setSoDienThoai(request.getSoDienThoai());
        }

        if (request.getDiaChi() != null) {
            khachHang.setDiaChi(request.getDiaChi());
        }

        khachHangRepository.save(khachHang);
    }

    public void changePassword(ChangePasswordRequest request) throws Exception {
        KhachHang khachHang = getCurrentUser();

        // 1. Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getMatKhauCu(), khachHang.getMatKhau())) {
            throw new Exception("BAD_REQUEST:Mật khẩu cũ không chính xác");
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận
        if (!request.getMatKhauMoi().equals(request.getXacNhanMatKhauMoi())) {
            throw new Exception("BAD_REQUEST:Mật khẩu xác nhận không khớp");
        }

        // 3. Mã hóa và lưu
        khachHang.setMatKhau(passwordEncoder.encode(request.getMatKhauMoi()));
        khachHangRepository.save(khachHang);
    }
}