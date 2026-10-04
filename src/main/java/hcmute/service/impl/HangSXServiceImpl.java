// File: HangSXServiceImpl.java
package hcmute.service.impl;

import hcmute.dto.request.HangSXRequest;
import hcmute.dto.response.HangSXResponse;
import hcmute.entity.HangSX;
import hcmute.exception.CustomException;
import hcmute.repository.HangSXRepository;
import hcmute.service.CloudinaryService;
import hcmute.service.HangSXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import hcmute.dto.response.HangSXResponse;
import hcmute.entity.HangSX;
import hcmute.exception.CustomException;
import hcmute.repository.HangSXRepository;
import hcmute.service.HangSXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HangSXServiceImpl implements HangSXService {

    @Autowired
    private HangSXRepository hangSXRepository;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public List<HangSXResponse> getAll() {
        return hangSXRepository.findAll().stream()
                .map(hang -> new HangSXResponse(hang.getMaHang(), hang.getTenHang(), hang.getLogo()))
                .collect(Collectors.toList());
    }

    @Override
    public HangSXResponse getById(Long id) {
        HangSX hangSX = hangSXRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy hãng sản xuất", HttpStatus.NOT_FOUND));
        return new HangSXResponse(hangSX.getMaHang(), hangSX.getTenHang(), hangSX.getLogo());
    }

    @Override
    @Transactional
    public HangSXResponse create(HangSXRequest request) {
        if (hangSXRepository.existsByTenHang(request.getTenHang())) {
            throw new CustomException("Tên hãng sản xuất đã tồn tại", HttpStatus.BAD_REQUEST);
        }

        HangSX hangSX = new HangSX();
        hangSX.setTenHang(request.getTenHang());
        
     // Xử lý upload ảnh nếu có file gửi lên
        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(request.getLogoFile());
                hangSX.setLogo(imageUrl);
            } catch (IOException e) {
                throw new CustomException("Lỗi khi upload ảnh Hãng SX lên Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        HangSX savedHang = hangSXRepository.save(hangSX);
        return new HangSXResponse(savedHang.getMaHang(), savedHang.getTenHang(), savedHang.getLogo());
    }

    @Override
    @Transactional
    public HangSXResponse update(Long id, HangSXRequest request) {
        HangSX hangSX = hangSXRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy hãng sản xuất", HttpStatus.NOT_FOUND));

        if (hangSXRepository.existsByTenHangAndMaHangNot(request.getTenHang(), id)) {
            throw new CustomException("Tên hãng sản xuất đã tồn tại", HttpStatus.BAD_REQUEST);
        }

        hangSX.setTenHang(request.getTenHang());
        
     // Xử lý logic upload ảnh mới: Đè ảnh cũ nếu có file mới
        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            try {
                String newImageUrl = cloudinaryService.uploadImage(request.getLogoFile());
                hangSX.setLogo(newImageUrl); // Cập nhật URL mới
            } catch (IOException e) {
                throw new CustomException("Lỗi khi upload ảnh Hãng SX lên Cloudinary", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        // Nếu getLogoFile() == null hoặc rỗng -> Không làm gì cả, giữ nguyên URL cũ trong entity.

        HangSX updatedHang = hangSXRepository.save(hangSX);
        return new HangSXResponse(updatedHang.getMaHang(), updatedHang.getTenHang(), updatedHang.getLogo());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        HangSX hangSX = hangSXRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy hãng sản xuất", HttpStatus.NOT_FOUND));

        if (hangSX.getSanPhams() != null && !hangSX.getSanPhams().isEmpty()) {
            throw new CustomException("Không thể xóa hãng sản xuất đang chứa sản phẩm", HttpStatus.CONFLICT);
        }

        hangSXRepository.delete(hangSX);
    }
}