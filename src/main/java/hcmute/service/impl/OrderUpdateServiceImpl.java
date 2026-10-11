//OrderUpdateServiceImpl.java
package hcmute.service.impl;

import hcmute.enums.OrderStatus;
import hcmute.entity.ChiTietDonHang;
import hcmute.entity.DonHang;
import hcmute.entity.KhachHang;
import hcmute.entity.KhoHang;
import hcmute.entity.NhanVien;
import hcmute.exception.CustomException;
import hcmute.repository.DonHangRepository;
import hcmute.repository.KhachHangRepository;
import hcmute.repository.KhoHangRepository;
import hcmute.repository.NhanVienRepository;
import hcmute.service.OrderUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderUpdateServiceImpl implements OrderUpdateService {

    @Autowired private DonHangRepository donHangRepository;
    @Autowired private NhanVienRepository nhanVienRepository;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private KhoHangRepository khoHangRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(String email, Long maDH, OrderStatus newStatus) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Không tìm thấy nhân viên.", HttpStatus.NOT_FOUND));

        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new CustomException("Không tìm thấy đơn hàng.", HttpStatus.NOT_FOUND));

        // Kiểm tra quyền hạn (Data Isolation)
        String vaiTro = nhanVien.getVaiTro() != null ? nhanVien.getVaiTro().trim() : "";
        if (!"Admin".equalsIgnoreCase(vaiTro)) {
            if (nhanVien.getChiNhanh() == null || !nhanVien.getChiNhanh().getMaCN().equals(donHang.getChiNhanh().getMaCN())) {
                throw new CustomException("Bạn không có quyền cập nhật đơn hàng của chi nhánh khác.", HttpStatus.FORBIDDEN);
            }
        }

        // Rẽ nhánh logic Hủy đơn
        if (newStatus == OrderStatus.CANCELLED) {
            cancelOrder(email, maDH, true);
            return; // Hàm cancelOrder đã tự xử lý save và hoàn tiền/kho
        }

        // Không cho phép đổi trạng thái của đơn đã Hủy hoặc Hoàn thành
        if (donHang.getTrangThai() == OrderStatus.CANCELLED || donHang.getTrangThai() == OrderStatus.COMPLETED) {
            throw new CustomException("Không thể thay đổi trạng thái của đơn hàng đã Hủy hoặc Hoàn thành.", HttpStatus.BAD_REQUEST);
        }

        donHang.setTrangThai(newStatus);
        donHangRepository.save(donHang);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String email, Long maDH, boolean isStaff) {
        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new CustomException("Không tìm thấy đơn hàng.", HttpStatus.NOT_FOUND));

        // Xác thực quyền Hủy
        if (isStaff) {
            NhanVien nhanVien = nhanVienRepository.findByEmail(email).orElseThrow();
            String vaiTro = nhanVien.getVaiTro() != null ? nhanVien.getVaiTro().trim() : "";
            if (!"Admin".equalsIgnoreCase(vaiTro) && 
                (nhanVien.getChiNhanh() == null || !nhanVien.getChiNhanh().getMaCN().equals(donHang.getChiNhanh().getMaCN()))) {
                throw new CustomException("Bạn không có quyền hủy đơn hàng của chi nhánh khác.", HttpStatus.FORBIDDEN);
            }
        } else {
            KhachHang khachHang = khachHangRepository.findByEmail(email).orElseThrow();
            if (!donHang.getKhachHang().getMaKH().equals(khachHang.getMaKH())) {
                throw new CustomException("Bạn không có quyền hủy đơn hàng của người khác.", HttpStatus.FORBIDDEN);
            }
        }

        // Logic Nghiệp vụ: Chỉ cho phép hủy khi đang PENDING
        if (donHang.getTrangThai() != OrderStatus.PENDING) {
            throw new CustomException("Chỉ có thể hủy đơn hàng khi đang ở trạng thái Chờ duyệt.", HttpStatus.BAD_REQUEST);
        }

        // 1. Cập nhật trạng thái
        donHang.setTrangThai(OrderStatus.CANCELLED);
        donHangRepository.save(donHang);

        // 2. Hoàn Tồn Kho (Dùng lại Khóa bi quan để tránh đụng độ với luồng Checkout đang chạy song song)
        Long maCN = donHang.getChiNhanh().getMaCN();
        for (ChiTietDonHang ct : donHang.getChiTietDonHangs()) {
            Long maSP = ct.getSanPham().getMaSP();
            KhoHang khoHang = khoHangRepository.findByIdWithPessimisticLock(maSP, maCN)
                    .orElseThrow(() -> new CustomException("Lỗi dữ liệu kho hàng.", HttpStatus.INTERNAL_SERVER_ERROR));
            
            khoHang.setSoLuongTon(khoHang.getSoLuongTon() + ct.getSoLuong());
            khoHangRepository.save(khoHang);
        }

        // 3. Hoàn Tiền Ví Khách Hàng
        KhachHang khachHangMua = donHang.getKhachHang();
        khachHangMua.setSoDuVi(khachHangMua.getSoDuVi().add(donHang.getTongTien()));
        khachHangRepository.save(khachHangMua);
    }
}