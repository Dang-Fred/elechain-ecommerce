// File: DanhMucServiceImpl.java
package hcmute.service.impl;

import hcmute.dto.request.DanhMucRequest;
import hcmute.dto.response.DanhMucResponse;
import hcmute.entity.DanhMuc;
import hcmute.service.CloudinaryService;
import hcmute.exception.CustomException;
import hcmute.repository.DanhMucRepository;
import hcmute.service.DanhMucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DanhMucServiceImpl implements DanhMucService {

    @Autowired
    private DanhMucRepository danhMucRepository;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public List<DanhMucResponse> getAll() {
        return danhMucRepository.findAll().stream()
                .map(dm -> new DanhMucResponse(dm.getMaDM(), dm.getTenDM(),dm.getLogo()))
                .collect(Collectors.toList());
    }

    @Override
    public DanhMucResponse getById(Long id) {
        DanhMuc dm = danhMucRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy danh mục", HttpStatus.NOT_FOUND));
        return new DanhMucResponse(dm.getMaDM(), dm.getTenDM(),dm.getLogo());
    }

    @Override
    @Transactional
    public DanhMucResponse create(DanhMucRequest request) {
        // UC 22: Kiểm tra trùng lặp Tên Danh Mục (HTTP 400)
        if (danhMucRepository.existsByTenDM(request.getTenDM())) {
            throw new CustomException("Tên danh mục đã tồn tại", HttpStatus.BAD_REQUEST);
        }

        DanhMuc danhMuc = new DanhMuc();
        danhMuc.setTenDM(request.getTenDM());
        
        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(request.getLogoFile());
                danhMuc.setLogo(imageUrl);
            } catch (IOException e) {
                throw new CustomException("Lỗi upload ảnh Danh mục", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
        DanhMuc savedDm = danhMucRepository.save(danhMuc);
        return new DanhMucResponse(savedDm.getMaDM(), savedDm.getTenDM(),savedDm.getLogo());
    }

    @Override
    @Transactional
    public DanhMucResponse update(Long id, DanhMucRequest request) {
        DanhMuc danhMuc = danhMucRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy danh mục", HttpStatus.NOT_FOUND));

        // UC 23: Kiểm tra trùng lặp Tên Danh Mục nhưng bỏ qua chính danh mục hiện tại (HTTP 400)
        if (danhMucRepository.existsByTenDMAndMaDMNot(request.getTenDM(), id)) {
            throw new CustomException("Tên danh mục đã tồn tại", HttpStatus.BAD_REQUEST);
        }

        danhMuc.setTenDM(request.getTenDM());
        
     // Giữ nguyên logic update: Có file thì đè URL, không thì giữ nguyên
        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            try {
                String newImageUrl = cloudinaryService.uploadImage(request.getLogoFile());
                danhMuc.setLogo(newImageUrl);
            } catch (IOException e) {
                throw new CustomException("Lỗi upload ảnh Danh mục", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
        DanhMuc updatedDm = danhMucRepository.save(danhMuc);
        return new DanhMucResponse(updatedDm.getMaDM(), updatedDm.getTenDM(),updatedDm.getLogo());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DanhMuc danhMuc = danhMucRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy danh mục", HttpStatus.NOT_FOUND));

        // UC 24: Kiểm tra danh mục có đang chứa sản phẩm hay không (HTTP 409)
        if (danhMuc.getSanPhams() != null && !danhMuc.getSanPhams().isEmpty()) {
            throw new CustomException("Không thể xóa danh mục đang có sản phẩm", HttpStatus.CONFLICT);
        }

        danhMucRepository.delete(danhMuc);
    }
}