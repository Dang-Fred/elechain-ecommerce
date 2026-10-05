// File: WalletService.java
package hcmute.service;

import hcmute.dto.request.TopupRequest;
import hcmute.entity.KhachHang;
import hcmute.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    private KhachHang getCurrentUser() throws Exception {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("UNAUTHORIZED:Không tìm thấy thông tin tài khoản"));
    }

    // Xem số dư
    public BigDecimal getBalance() throws Exception {
        KhachHang khachHang = getCurrentUser();
        return khachHang.getSoDuVi() != null ? khachHang.getSoDuVi() : BigDecimal.ZERO; 
    }

    // Nạp tiền
    @Transactional
    public BigDecimal topup(TopupRequest request) throws Exception {
        KhachHang khachHang = getCurrentUser();
        BigDecimal soTienNap = request.getSoTien();

        // Validate: soTienNap <= 0
        if (soTienNap == null || soTienNap.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("BAD_REQUEST:Số tiền nạp phải lớn hơn 0");
        }

        BigDecimal soDuHienTai = khachHang.getSoDuVi() != null ? khachHang.getSoDuVi() : BigDecimal.ZERO;
        
        // Tính toán: soDuMoi = soDuHienTai + soTienNap
        BigDecimal soDuMoi = soDuHienTai.add(soTienNap);

        // Đảm bảo số dư không âm (soDuMoi < 0)
        if (soDuMoi.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("BAD_REQUEST:Số dư không hợp lệ");
        }

        // Cập nhật và lưu DB
        khachHang.setSoDuVi(soDuMoi);
        khachHangRepository.save(khachHang);

        return soDuMoi;
    }
}