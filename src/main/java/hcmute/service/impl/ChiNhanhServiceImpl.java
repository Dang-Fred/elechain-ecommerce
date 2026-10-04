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

@Service
public class ChiNhanhServiceImpl implements ChiNhanhService {

    @Autowired
    private ChiNhanhRepository chiNhanhRepository;

    @Override
    @Transactional
    public ChiNhanhResponse create(ChiNhanhRequest request) {
        // Kiểm tra trùng lặp Tên chi nhánh & Hotline
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

        // TODO: UC 31 (Bước 4) - Gọi hàm Khởi tạo Kho Hàng trống ở đây khi có KhoHangRepository

        return new ChiNhanhResponse(savedChiNhanh.getMaCN(), savedChiNhanh.getTenCN(), 
                                    savedChiNhanh.getDiaChi(), savedChiNhanh.getHotline());
    }

    @Override
    @Transactional
    public ChiNhanhResponse update(Long id, ChiNhanhRequest request) {
        ChiNhanh chiNhanh = chiNhanhRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy chi nhánh", HttpStatus.NOT_FOUND));

        // Kiểm tra trùng lặp (loại trừ chính nó)
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

        return new ChiNhanhResponse(updatedChiNhanh.getMaCN(), updatedChiNhanh.getTenCN(), 
                                    updatedChiNhanh.getDiaChi(), updatedChiNhanh.getHotline());
    }
    

    @Override
    public java.util.List<ChiNhanhResponse> getAllActive() {
        return chiNhanhRepository.findAllByTrangThaiTrue().stream()
                .map(cn -> new ChiNhanhResponse(cn.getMaCN(), cn.getTenCN(), cn.getDiaChi(), cn.getHotline()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public ChiNhanhResponse getActiveById(Long id) {
        ChiNhanh chiNhanh = chiNhanhRepository.findByMaCNAndTrangThaiTrue(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy chi nhánh hoặc chi nhánh đã ngừng hoạt động", HttpStatus.NOT_FOUND));
        
        return new ChiNhanhResponse(chiNhanh.getMaCN(), chiNhanh.getTenCN(), chiNhanh.getDiaChi(), chiNhanh.getHotline());
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        // Tìm chi nhánh, nếu không tồn tại hoặc đã xóa mềm thì báo 404
        ChiNhanh chiNhanh = chiNhanhRepository.findByMaCNAndTrangThaiTrue(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy chi nhánh hoặc chi nhánh đã ngừng hoạt động", HttpStatus.NOT_FOUND));
        
        // Đổi trạng thái thay vì xóa vật lý
        chiNhanh.setTrangThai(false);
        chiNhanhRepository.save(chiNhanh);
    }
    
    @Override
    @Transactional
    public void restore(Long id) {
        // Dùng findById mặc định để tìm được cả những chi nhánh đang có trangThai = false
        ChiNhanh chiNhanh = chiNhanhRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy chi nhánh", HttpStatus.NOT_FOUND));

        if (chiNhanh.getTrangThai()) {
            throw new CustomException("Chi nhánh này vẫn đang hoạt động, không cần khôi phục!", HttpStatus.BAD_REQUEST);
        }

        // Bật lại trạng thái hoạt động
        chiNhanh.setTrangThai(true);
        chiNhanhRepository.save(chiNhanh);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}