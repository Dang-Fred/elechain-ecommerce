// File: SanPhamServiceImpl.java
package hcmute.service.impl;
import hcmute.dto.request.SanPhamRequest;
import hcmute.dto.response.SanPhamResponse;
import hcmute.entity.DanhMuc;
import hcmute.entity.HangSX;
import hcmute.entity.SanPham;
import hcmute.exception.CustomException;
import hcmute.repository.DanhMucRepository;
import hcmute.repository.HangSXRepository;
import hcmute.repository.SanPhamRepository;
import hcmute.service.CloudinaryService;
import hcmute.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class SanPhamServiceImpl implements SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private HangSXRepository hangSXRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public SanPhamResponse create(SanPhamRequest request) {
        // 1. Kiểm tra Khóa ngoại (Ném lỗi 400 nếu không tồn tại)
        DanhMuc danhMuc = danhMucRepository.findById(request.getMaDM())
                .orElseThrow(() -> new CustomException("Danh mục không tồn tại trong hệ thống", HttpStatus.BAD_REQUEST));
        
        HangSX hangSX = hangSXRepository.findById(request.getMaHang())
                .orElseThrow(() -> new CustomException("Hãng sản xuất không tồn tại trong hệ thống", HttpStatus.BAD_REQUEST));

        // 2. Map dữ liệu cơ bản
        SanPham sanPham = new SanPham();
        sanPham.setTenSP(request.getTenSP());
        sanPham.setDanhMuc(danhMuc);
        sanPham.setHangSX(hangSX);
        sanPham.setGiaBan(request.getGiaBan());
        sanPham.setBaoHanh(request.getBaoHanh());
        sanPham.setCauHinh(request.getCauHinh());
        sanPham.setMoTa(request.getMoTa());
        
        // Mặc định gán Trạng thái là "true" (Đang kinh doanh)
        sanPham.setTrangThai("true");

        // 3. Xử lý upload ảnh (nếu có)
        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(request.getLogoFile());
                sanPham.setLogo(imageUrl);
            } catch (IOException e) {
                throw new CustomException("Lỗi khi upload ảnh sản phẩm lên Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        SanPham savedSP = sanPhamRepository.save(sanPham);
        return mapToResponse(savedSP);
    }

    @Override
    @Transactional
    public SanPhamResponse update(Long id, SanPhamRequest request) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy sản phẩm", HttpStatus.NOT_FOUND));

        // 1. Kiểm tra Khóa ngoại
        DanhMuc danhMuc = danhMucRepository.findById(request.getMaDM())
                .orElseThrow(() -> new CustomException("Danh mục không tồn tại trong hệ thống", HttpStatus.BAD_REQUEST));
        
        HangSX hangSX = hangSXRepository.findById(request.getMaHang())
                .orElseThrow(() -> new CustomException("Hãng sản xuất không tồn tại trong hệ thống", HttpStatus.BAD_REQUEST));

        // 2. Cập nhật dữ liệu cơ bản
        sanPham.setTenSP(request.getTenSP());
        sanPham.setDanhMuc(danhMuc);
        sanPham.setHangSX(hangSX);
        sanPham.setGiaBan(request.getGiaBan());
        sanPham.setBaoHanh(request.getBaoHanh());
        sanPham.setCauHinh(request.getCauHinh());
        sanPham.setMoTa(request.getMoTa());

        // 3. Logic Update ảnh: Nếu có file gửi lên -> Upload đè URL mới. Nếu null -> Giữ URL cũ.
        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            try {
                String newImageUrl = cloudinaryService.uploadImage(request.getLogoFile());
                sanPham.setLogo(newImageUrl);
            } catch (IOException e) {
                throw new CustomException("Lỗi khi upload ảnh sản phẩm lên Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        SanPham updatedSP = sanPhamRepository.save(sanPham);
        return mapToResponse(updatedSP);
    }

    // Hàm tiện ích để Map từ Entity sang DTO Response
    private SanPhamResponse mapToResponse(SanPham sanPham) {
        return new SanPhamResponse(
                sanPham.getMaSP(),
                sanPham.getTenSP(),
                sanPham.getDanhMuc().getMaDM(),
                sanPham.getDanhMuc().getTenDM(),
                sanPham.getHangSX().getMaHang(),
                sanPham.getHangSX().getTenHang(),
                sanPham.getGiaBan(),
                sanPham.getBaoHanh(),
                sanPham.getCauHinh(),
                sanPham.getMoTa(),
                sanPham.getTrangThai(),
                sanPham.getLogo()
        );
    }
}