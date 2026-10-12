//ReviewServiceImpl.java
package hcmute.service.impl;

import hcmute.dto.request.ReviewRequestDTO;
import hcmute.dto.response.ReviewResponseDTO;
import hcmute.entity.DanhGia;
import hcmute.entity.DanhGiaKey;
import hcmute.entity.DonHang;
import hcmute.entity.KhachHang;
import hcmute.entity.SanPham;
import hcmute.enums.OrderStatus;
import hcmute.exception.CustomException;
import hcmute.exception.IllegalReviewException;
import hcmute.repository.DanhGiaRepository;
import hcmute.repository.DonHangRepository;
import hcmute.repository.KhachHangRepository;
import hcmute.repository.SanPhamRepository;
import hcmute.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired private DanhGiaRepository danhGiaRepository;
    @Autowired private DonHangRepository donHangRepository;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private SanPhamRepository sanPhamRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResponseDTO taoDanhGia(String email, ReviewRequestDTO request) {
        KhachHang khachHang = khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Không tìm thấy khách hàng.", HttpStatus.NOT_FOUND));
        
        Long maKH = khachHang.getMaKH();
        Long maSP = request.getMaSP();
        Long maDH = request.getMaDH();

        // 1. Tìm đơn hàng và kiểm tra quyền sở hữu
        DonHang donHang = donHangRepository.findById(maDH)
                .orElseThrow(() -> new CustomException("Không tìm thấy đơn hàng.", HttpStatus.NOT_FOUND));

        if (!donHang.getKhachHang().getMaKH().equals(maKH)) {
            throw new CustomException("Bạn không có quyền đánh giá đơn hàng của người khác.", HttpStatus.FORBIDDEN);
        }

     // 2. Kiểm tra trạng thái đơn hàng
        if (donHang.getTrangThai() != OrderStatus.COMPLETED) {
            throw new IllegalReviewException("Chỉ có thể đánh giá khi đơn hàng đã hoàn thành.");
        }
        
     // 3. Kiểm tra sản phẩm có nằm trong đơn hàng này không
        boolean hasProduct = donHang.getChiTietDonHangs().stream()
                .anyMatch(ct -> ct.getSanPham().getMaSP().equals(maSP));
        
        if (!hasProduct) {
            throw new IllegalReviewException("Sản phẩm này không nằm trong đơn hàng của bạn.");
        }

        // 4. Chống Spam: Trong cùng 1 đơn hàng, 1 sản phẩm chỉ được đánh giá 1 lần
        DanhGiaKey key = new DanhGiaKey(maKH, maSP, maDH);
        if (danhGiaRepository.existsById(key)) {
            throw new CustomException("Bạn đã đánh giá sản phẩm này trong đơn hàng " + maDH + " rồi.", HttpStatus.BAD_REQUEST);
        }

        SanPham sanPham = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new CustomException("Không tìm thấy sản phẩm.", HttpStatus.NOT_FOUND));

        // 5. Khởi tạo và Lưu dữ liệu
        DanhGia danhGia = new DanhGia();
        danhGia.setId(key);
        danhGia.setKhachHang(khachHang);
        danhGia.setSanPham(sanPham);
        danhGia.setDonHang(donHang); // SET thêm Đơn hàng
        danhGia.setSoSao(request.getSoSao());
        danhGia.setBinhLuan(request.getBinhLuan());
        danhGia.setNgayDG(LocalDateTime.now());

        danhGiaRepository.save(danhGia);

        return new ReviewResponseDTO(
                khachHang.getTenKH(),
                khachHang.getAvatar(),
                danhGia.getSoSao(),
                danhGia.getBinhLuan(),
                danhGia.getNgayDG()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByProduct(Long maSP, Pageable pageable) {
        Page<DanhGia> danhGias = danhGiaRepository.findBySanPham_MaSPOrderByNgayDGDesc(maSP, pageable);
        
        // Map Page<Entity> sang Page<DTO>
        return danhGias.map(dg -> new ReviewResponseDTO(
                dg.getKhachHang().getTenKH(),
                dg.getKhachHang().getAvatar(),
                dg.getSoSao(),
                dg.getBinhLuan(),
                dg.getNgayDG()
        ));
    }
}
