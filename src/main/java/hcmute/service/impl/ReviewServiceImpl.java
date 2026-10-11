//ReviewServiceImpl.java
package hcmute.service.impl;

import hcmute.dto.request.ReviewRequestDTO;
import hcmute.dto.response.ReviewResponseDTO;
import hcmute.entity.DanhGia;
import hcmute.entity.DanhGiaKey;
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

        // Bước 1: Xác thực mua hàng (Đơn hàng phải COMPLETED)
        if (!donHangRepository.existsCompletedOrderByKhachHangAndSanPham(maKH, maSP, OrderStatus.COMPLETED)) {
            // Ném Custom Exception theo đúng yêu cầu
            throw new IllegalReviewException("Bạn chưa mua sản phẩm này hoặc đơn hàng chưa hoàn thành.");
        }

        // Bước 2: Chống Spam đánh giá
        if (danhGiaRepository.existsById_MaKHAndId_MaSP(maKH, maSP)) {
            throw new CustomException("Bạn đã đánh giá sản phẩm này rồi.", HttpStatus.BAD_REQUEST);
        }

        SanPham sanPham = sanPhamRepository.findById(maSP)
                .orElseThrow(() -> new CustomException("Không tìm thấy sản phẩm.", HttpStatus.NOT_FOUND));

        // Bước 3: Ghi dữ liệu
        DanhGia danhGia = new DanhGia();
        danhGia.setId(new DanhGiaKey(maKH, maSP));
        danhGia.setKhachHang(khachHang);
        danhGia.setSanPham(sanPham);
        danhGia.setSoSao(request.getSoSao());
        danhGia.setBinhLuan(request.getBinhLuan());
        danhGia.setNgayDG(LocalDateTime.now());

        danhGiaRepository.save(danhGia);

        // Trả về DTO
        return new ReviewResponseDTO(
                khachHang.getTenKH(),
                khachHang.getAvatar(), // Giả sử bảng KhachHang có trường avatar
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
