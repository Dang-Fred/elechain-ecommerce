//File: InventoryService.java
package hcmute.service;

import hcmute.dto.request.ChiTietNhapRequest;
import hcmute.dto.request.PhieuNhapRequest;
import hcmute.dto.response.PhieuNhapResponse;
import hcmute.entity.*;
import hcmute.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final PhieuNhapRepository phieuNhapRepository;
    private final ChiTietPhieuNhapRepository chiTietPhieuNhapRepository;
    private final KhoHangRepository khoHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final SanPhamRepository sanPhamRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createPhieuNhap(String email, PhieuNhapRequest request) {
        try {
            // Bước 1: Tìm nhân viên đang đăng nhập
            NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên (Email: " + email + ")"));

            if (nhanVien.getChiNhanh() == null) {
                throw new RuntimeException("Nhân viên chưa được phân bổ về chi nhánh nào!");
            }

            // Bước 2: Khởi tạo và lưu Phiếu Nhập
            PhieuNhap phieuNhap = new PhieuNhap();
            phieuNhap.setNhanVien(nhanVien);
            phieuNhap.setChiNhanh(nhanVien.getChiNhanh());
            phieuNhap.setNgayLap(LocalDateTime.now()); // Tuân thủ RBTV15: Ngày lập <= Ngày hiện tại
            PhieuNhap savedPhieuNhap = phieuNhapRepository.save(phieuNhap);

            // Bước 3: Xử lý danh sách chi tiết phiếu nhập
            for (ChiTietNhapRequest ctReq : request.getDanhSachSanPham()) {
                SanPham sanPham = sanPhamRepository.findById(ctReq.getMaSP())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + ctReq.getMaSP()));

                // Lưu Chi Tiết Phiếu Nhập
                ChiTietPhieuNhap ctPhieuNhap = new ChiTietPhieuNhap();
                ctPhieuNhap.setId(new ChiTietPhieuNhapKey(savedPhieuNhap.getMaPN(), sanPham.getMaSP()));
                ctPhieuNhap.setPhieuNhap(savedPhieuNhap);
                ctPhieuNhap.setSanPham(sanPham);
                ctPhieuNhap.setSoLuongNhap(ctReq.getSoLuongNhap());
                chiTietPhieuNhapRepository.save(ctPhieuNhap);

                // Cập nhật Kho Hàng (Cộng dồn số lượng tồn)
                KhoHang khoHang = khoHangRepository.findById_MaSPAndId_MaCN(sanPham.getMaSP(), nhanVien.getChiNhanh().getMaCN())
                        .orElseGet(() -> {
                            KhoHang newKho = new KhoHang();
                            newKho.setId(new KhoHangKey(sanPham.getMaSP(), nhanVien.getChiNhanh().getMaCN()));
                            newKho.setSoLuongTon(0);
                            return newKho;
                        });
                
                khoHang.setSoLuongTon(khoHang.getSoLuongTon() + ctReq.getSoLuongNhap());
                khoHangRepository.save(khoHang);
            }
        } catch (Exception e) {
            // Ném lỗi ra để Spring Boot tự động Rollback lại toàn bộ transaction (bảng PhieuNhap, ChiTiet, KhoHang)
            throw new RuntimeException("Lỗi hệ thống khi nhập kho: " + e.getMessage(), e);
        }
    }

    public List<PhieuNhapResponse> getLichSuNhapKho(String email) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        List<PhieuNhap> danhSachPhieu = phieuNhapRepository.findByChiNhanh_MaCN(nhanVien.getChiNhanh().getMaCN());

        return danhSachPhieu.stream().map(pn -> {
            PhieuNhapResponse response = new PhieuNhapResponse();
            response.setMaPN(pn.getMaPN());
            response.setNgayLap(pn.getNgayLap());
            response.setTenNhanVien(pn.getNhanVien().getTenNV());

            List<PhieuNhapResponse.ChiTietSanPhamResponse> chiTietList = pn.getChiTietPhieuNhaps().stream()
                    .map(ct -> new PhieuNhapResponse.ChiTietSanPhamResponse(
                            ct.getSanPham().getMaSP(),
                            ct.getSanPham().getTenSP(),
                            ct.getSoLuongNhap()
                    )).collect(Collectors.toList());

            response.setDanhSachSanPham(chiTietList);
            return response;
        }).collect(Collectors.toList());
    }
}