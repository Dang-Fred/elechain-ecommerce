// File: OrderServiceImpl.java
package hcmute.service.impl;

import hcmute.dto.request.CheckoutRequestDTO;
import hcmute.dto.response.CheckoutResponseDTO;
import hcmute.entity.*;
import hcmute.exception.CustomException;
import hcmute.exception.InsufficientBalanceException;
import hcmute.exception.OutOfStockException;
import hcmute.repository.*;
import hcmute.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private GioHangRepository gioHangRepository;
    @Autowired private KhoHangRepository khoHangRepository;
    @Autowired private ChiNhanhRepository chiNhanhRepository;
    @Autowired private DonHangRepository donHangRepository; // Cần tạo thêm Repository này
    @Autowired private ChiTietDonHangRepository chiTietDonHangRepository; // Cần tạo thêm Repository này

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckoutResponseDTO checkout(String email, CheckoutRequestDTO request) {
        // 1. Tìm User & Giỏ Hàng
        KhachHang khachHang = khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Không tìm thấy tài khoản khách hàng.", HttpStatus.NOT_FOUND));
        
        List<GioHang> gioHangs = gioHangRepository.findById_MaKH(khachHang.getMaKH());
        if (gioHangs.isEmpty()) {
            throw new CustomException("Giỏ hàng trống, không thể thanh toán.", HttpStatus.BAD_REQUEST);
        }

        // 2. Tính Tổng Tiền (Lặp qua Giỏ hàng, lấy số lượng * giá bán)
        BigDecimal tongTien = BigDecimal.ZERO;
        for (GioHang gh : gioHangs) {
            BigDecimal subTotal = gh.getSanPham().getGiaBan().multiply(new BigDecimal(gh.getSoLuong()));
            tongTien = tongTien.add(subTotal);
        }

        // 3. Thanh Toán Ví
        if (khachHang.getSoDuVi().compareTo(tongTien) < 0) {
            throw new InsufficientBalanceException("Số dư ví không đủ để thanh toán. Cần: " + tongTien + ", Hiện có: " + khachHang.getSoDuVi());
        }
        // Trừ tiền và lưu lại
        khachHang.setSoDuVi(khachHang.getSoDuVi().subtract(tongTien));
        khachHangRepository.save(khachHang);

        // 4. Khởi tạo Đơn Hàng
        ChiNhanh chiNhanh = chiNhanhRepository.findById(request.getMaCN())
                .orElseThrow(() -> new CustomException("Chi nhánh không tồn tại.", HttpStatus.BAD_REQUEST));

        DonHang donHang = new DonHang();
        donHang.setKhachHang(khachHang);
        donHang.setChiNhanh(chiNhanh);
        donHang.setNgayDat(LocalDateTime.now());
        donHang.setPhuongThucNhan(request.getPhuongThucNhan());
        donHang.setTrangThai("Chờ duyệt");
        donHang.setTongTien(tongTien);
        
        // Save để sinh maDH
        donHang = donHangRepository.save(donHang);

        // 5. Trừ Tồn Kho & Chốt Chi Tiết Đơn (CHỐNG BÁN ÂM VỚI LOCKING)
        List<ChiTietDonHang> chiTietList = new ArrayList<>();
        
        for (GioHang gh : gioHangs) {
            Long maSP = gh.getSanPham().getMaSP();
            
            // Lấy KhoHang bằng Khóa Bi Quan (Pessimistic Write)
            KhoHang khoHang = khoHangRepository.findByIdWithPessimisticLock(maSP, request.getMaCN())
                    .orElseThrow(() -> new CustomException("Sản phẩm " + gh.getSanPham().getTenSP() + " không kinh doanh tại chi nhánh này.", HttpStatus.BAD_REQUEST));
            
            // Chống bán âm
            if (khoHang.getSoLuongTon() < gh.getSoLuong()) {
                throw new OutOfStockException("Sản phẩm '" + gh.getSanPham().getTenSP() + "' không đủ số lượng tồn kho (Chỉ còn " + khoHang.getSoLuongTon() + ").");
            }
            
            // Trừ tồn kho và lưu
            khoHang.setSoLuongTon(khoHang.getSoLuongTon() - gh.getSoLuong());
            khoHangRepository.save(khoHang);

            // Chốt ChiTietDonHang
            ChiTietDonHang ct = new ChiTietDonHang();
            // Giả định ChiTietDonHangKey(maDH, maSP)
            ct.setId(new ChiTietDonHangKey(donHang.getMaDH(), maSP)); 
            ct.setDonHang(donHang);
            ct.setSanPham(gh.getSanPham());
            ct.setSoLuong(gh.getSoLuong());
            ct.setDonGia(gh.getSanPham().getGiaBan()); // Chốt giá tại thời điểm đặt hàng
            
            chiTietList.add(ct);
        }
        
        // Lưu toàn bộ chi tiết đơn hàng
        chiTietDonHangRepository.saveAll(chiTietList);

        // 6. Dọn dẹp: Xóa sạch giỏ hàng của user sau khi đặt thành công
        gioHangRepository.deleteByKhachHang_MaKH(khachHang.getMaKH());

        // 7. Trả về DTO thông tin đơn hàng
        return new CheckoutResponseDTO(
                donHang.getMaDH(),
                donHang.getNgayDat(),
                donHang.getPhuongThucNhan(),
                donHang.getTrangThai(),
                donHang.getTongTien()
        );
    }
}