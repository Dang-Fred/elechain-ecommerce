// File: ChiNhanhServiceImpl.java
package hcmute.service.impl;

import hcmute.dto.request.ChiNhanhRequest;
import hcmute.dto.response.ChiNhanhResponse;
import hcmute.entity.ChiNhanh;
import hcmute.exception.CustomException;
import hcmute.repository.ChiNhanhRepository;
import hcmute.service.ChiNhanhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChiNhanhServiceImpl implements ChiNhanhService {

    @Autowired
    private ChiNhanhRepository chiNhanhRepository;

 // Hàm tiện ích dùng chung để map Entity -> DTO
    private ChiNhanhResponse mapToResponse(ChiNhanh chiNhanh) {
        return new ChiNhanhResponse(
                chiNhanh.getMaCN(), 
                chiNhanh.getTenCN(), 
                chiNhanh.getDiaChi(), 
                chiNhanh.getHotline(),
                chiNhanh.getTrangThai() // MỚI BỔ SUNG
        );
    }

    @Override
    @Transactional
    public ChiNhanhResponse create(ChiNhanhRequest request) {
        if (chiNhanhRepository.existsByTenCN(request.getTenCN())) {
            throw new CustomException("Tên chi nhánh đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST);
        }
        if (chiNhanhRepository.existsByHotline(request.getHotline())) {
            throw new CustomException("Hotline này đã được sử dụng cho chi nhánh khác", HttpStatus.BAD_REQUEST);
        }

        ChiNhanh chiNhanh = new ChiNhanh();
        chiNhanh.setTenCN(request.getTenCN());
        chiNhanh.setDiaChi(request.getDiaChi());
        chiNhanh.setHotline(request.getHotline());

        ChiNhanh savedChiNhanh = chiNhanhRepository.save(chiNhanh);

        return mapToResponse(savedChiNhanh);
    }

    @Override
    @Transactional
    public ChiNhanhResponse update(Long id, ChiNhanhRequest request) {
        ChiNhanh chiNhanh = chiNhanhRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy chi nhánh", HttpStatus.NOT_FOUND));

        if (chiNhanhRepository.existsByTenCNAndMaCNNot(request.getTenCN(), id)) {
            throw new CustomException("Tên chi nhánh đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST);
        }
        if (chiNhanhRepository.existsByHotlineAndMaCNNot(request.getHotline(), id)) {
            throw new CustomException("Hotline này đã được sử dụng cho chi nhánh khác", HttpStatus.BAD_REQUEST);
        }

        chiNhanh.setTenCN(request.getTenCN());
        chiNhanh.setDiaChi(request.getDiaChi());
        chiNhanh.setHotline(request.getHotline());

        ChiNhanh updatedChiNhanh = chiNhanhRepository.save(chiNhanh);

        return mapToResponse(updatedChiNhanh);
    }

    @Override
    public List<ChiNhanhResponse> getAllActive() {
        return chiNhanhRepository.findAllByTrangThaiTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ChiNhanhResponse getActiveById(Long id) {
        ChiNhanh chiNhanh = chiNhanhRepository.findByMaCNAndTrangThaiTrue(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy chi nhánh hoặc chi nhánh đã ngừng hoạt động", HttpStatus.NOT_FOUND));
        
        return mapToResponse(chiNhanh);
    }

    @Override
    public List<ChiNhanhResponse> getAll() {
        // Lấy TẤT CẢ chi nhánh (phục vụ cho Admin quản lý cả chi nhánh đã ẩn)
        return chiNhanhRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        ChiNhanh chiNhanh = chiNhanhRepository.findByMaCNAndTrangThaiTrue(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy chi nhánh hoặc chi nhánh đã ngừng hoạt động", HttpStatus.NOT_FOUND));
        
        chiNhanh.setTrangThai(false);
        chiNhanhRepository.save(chiNhanh);
    }
    
    @Override
    @Transactional
    public void restore(Long id) {
        ChiNhanh chiNhanh = chiNhanhRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy chi nhánh", HttpStatus.NOT_FOUND));

        if (chiNhanh.getTrangThai()) {
            throw new CustomException("Chi nhánh này vẫn đang hoạt động, không cần khôi phục!", HttpStatus.BAD_REQUEST);
        }

        chiNhanh.setTrangThai(true);
        chiNhanhRepository.save(chiNhanh);
    }
}