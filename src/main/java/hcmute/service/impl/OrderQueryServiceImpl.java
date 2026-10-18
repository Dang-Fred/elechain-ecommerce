// File: OrderQueryServiceImpl.java
package hcmute.service.impl;

import hcmute.dto.response.OrderDetailResponseDTO;
import hcmute.dto.response.OrderPageSummaryDTO;
import hcmute.dto.response.OrderResponseDTO;
import hcmute.entity.ChiTietDonHang;
import hcmute.entity.DonHang;
import hcmute.entity.KhachHang;
import hcmute.entity.NhanVien;
import hcmute.enums.OrderStatus;
import hcmute.exception.CustomException;
import hcmute.repository.ChiTietDonHangRepository;
import hcmute.repository.DonHangRepository;
import hcmute.repository.KhachHangRepository;
import hcmute.repository.NhanVienRepository;
import hcmute.service.OrderQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    @Autowired private DonHangRepository donHangRepository;
    @Autowired private ChiTietDonHangRepository chiTietDonHangRepository;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private NhanVienRepository nhanVienRepository;
    

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getMyOrders(String email, Pageable pageable) {
        KhachHang khachHang = khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Không tìm thấy khách hàng.", HttpStatus.NOT_FOUND));

        Page<DonHang> donHangs = donHangRepository.findByKhachHang_MaKHOrderByNgayDatDesc(khachHang.getMaKH(), pageable);
        
        // Trực tiếp map Page<Entity> sang Page<DTO>
        return donHangs.map(this::mapToOrderResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrdersByBranch(String email, Pageable pageable) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Không tìm thấy nhân viên.", HttpStatus.NOT_FOUND));

        Page<DonHang> donHangs;
        
        // 3. Fix lỗi NullPointerException & Phân luồng cho Admin
        if ("Admin".equalsIgnoreCase(nhanVien.getVaiTro())) {
            donHangs = donHangRepository.findAll(pageable); // Lấy toàn hệ thống
        } else {
            Long maCN = nhanVien.getChiNhanh().getMaCN(); 
            donHangs = donHangRepository.findByChiNhanh_MaCNOrderByNgayDatDesc(maCN, pageable);
        }
        
        return donHangs.map(this::mapToOrderResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderDetail(String email, Long maDH) {
        // 1. Tìm đơn hàng theo mã và email (Bảo mật Data Isolation)
        DonHang donHang = donHangRepository.findByMaDHAndKhachHang_Email(maDH, email)
                .orElseThrow(() -> new CustomException("Không tìm thấy đơn hàng hoặc bạn không có quyền xem.", HttpStatus.NOT_FOUND));

        return mapToOrderResponseDTO(donHang);
    }
    
    
    @Override
    @Transactional(readOnly = true)
    public OrderPageSummaryDTO getOrderPageSummary(String email) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Không tìm thấy nhân viên.", HttpStatus.NOT_FOUND));

        String vaiTro = nhanVien.getVaiTro() != null ? nhanVien.getVaiTro().trim() : "";
        
        if ("Admin".equalsIgnoreCase(vaiTro)) {
            // View Master cho Admin
            return OrderPageSummaryDTO.builder()
                    .totalOrders(donHangRepository.count())
                    .pendingOrders(donHangRepository.countByTrangThai(OrderStatus.PENDING))
                    .deliveringOrders(donHangRepository.countByTrangThai(OrderStatus.DELIVERING))
                    .completedOrders(donHangRepository.countByTrangThai(OrderStatus.COMPLETED))
                    .cancelledOrders(donHangRepository.countByTrangThai(OrderStatus.CANCELLED))
                    .build();
        } else {
            // Data Isolation cho Chi nhánh
            if (nhanVien.getChiNhanh() == null) {
                throw new CustomException("Lỗi dữ liệu: Nhân viên này không thuộc chi nhánh nào.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Long maCN = nhanVien.getChiNhanh().getMaCN();
            
            return OrderPageSummaryDTO.builder()
                    .totalOrders(donHangRepository.countByChiNhanh_MaCN(maCN))
                    .pendingOrders(donHangRepository.countByTrangThaiAndChiNhanh_MaCN(OrderStatus.PENDING, maCN))
                    .deliveringOrders(donHangRepository.countByTrangThaiAndChiNhanh_MaCN(OrderStatus.DELIVERING, maCN))
                    .completedOrders(donHangRepository.countByTrangThaiAndChiNhanh_MaCN(OrderStatus.COMPLETED, maCN))
                    .cancelledOrders(donHangRepository.countByTrangThaiAndChiNhanh_MaCN(OrderStatus.CANCELLED, maCN))
                    .build();
        }
    }
    
    
    
  
    

    // Hàm helper map Entity sang DTO
    private OrderResponseDTO mapToOrderResponseDTO(DonHang dh) {
        // Nhờ @EntityGraph, List chiTietDonHangs đã được kéo sẵn từ RAM, không sinh thêm câu Query N+1 nào
        List<OrderDetailResponseDTO> detailDTOs = dh.getChiTietDonHangs().stream().map(ct -> new OrderDetailResponseDTO(
                ct.getSanPham().getMaSP(),
                ct.getSanPham().getTenSP(),
                ct.getSoLuong(),
                ct.getDonGia(),
                ct.getSanPham().getLogo()
        )).collect(Collectors.toList());

        return new OrderResponseDTO(
                dh.getMaDH(),
                dh.getNgayDat(),
                dh.getPhuongThucNhan(),
                dh.getTrangThai().getDescription(), // Trả về text tiếng Việt (vd: "Chờ duyệt")
                dh.getTongTien(),
                dh.getKhachHang().getTenKH(),
                dh.getDiaChiGiao(), // Map từ trường snapshot thay vì bảng KhachHang
                detailDTOs
        );
    } 
    
}