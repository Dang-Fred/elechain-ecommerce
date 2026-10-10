// File: GioHangServiceImpl.java
package hcmute.service.impl;

import hcmute.dto.request.CartRequestDTO;
import hcmute.dto.response.CartItemResponseDTO;
import hcmute.entity.GioHang;
import hcmute.entity.GioHangKey;
import hcmute.entity.KhachHang;
import hcmute.entity.SanPham;
import hcmute.exception.CustomException;
import hcmute.repository.GioHangRepository;
import hcmute.repository.KhachHangRepository;
import hcmute.repository.KhoHangRepository;
import hcmute.repository.SanPhamRepository;
import hcmute.service.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GioHangServiceImpl implements GioHangService {

    @Autowired private GioHangRepository gioHangRepository;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private SanPhamRepository sanPhamRepository;
    @Autowired private KhoHangRepository khoHangRepository;

    private KhachHang getKhachHangByEmail(String email) {
        return khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Không tìm thấy tài khoản khách hàng.",HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public void themVaoGioHang(String email, CartRequestDTO request) {
        KhachHang khachHang = getKhachHangByEmail(email);
        Long maKH = khachHang.getMaKH();
        Long maSP = request.getMaSP();
        
        // Kiểm tra tổng tồn kho toàn chuỗi
        Long tongTonKho = khoHangRepository.getTotalTonKhoByMaSP(maSP);
        
        Optional<GioHang> gioHangOpt = gioHangRepository.findById_MaKHAndId_MaSP(maKH, maSP);
        if (gioHangOpt.isPresent()) {
            // Đã có trong giỏ -> Cộng dồn
            GioHang gioHang = gioHangOpt.get();
            int soLuongMoi = gioHang.getSoLuong() + request.getSoLuong();
            
            if (soLuongMoi > tongTonKho) {
                throw new CustomException("Số lượng yêu cầu vượt quá tổng tồn kho hệ thống (" + tongTonKho + ").", HttpStatus.BAD_REQUEST);
            }
            gioHang.setSoLuong(soLuongMoi);
            gioHangRepository.save(gioHang);
        } else {
            // Chưa có trong giỏ -> Thêm mới
            if (request.getSoLuong() > tongTonKho) {
                throw new CustomException("Số lượng yêu cầu vượt quá tổng tồn kho hệ thống (" + tongTonKho + ").",HttpStatus.BAD_REQUEST);
            }
            SanPham sanPham = sanPhamRepository.findById(maSP)
                    .orElseThrow(() -> new CustomException("Sản phẩm không tồn tại.",HttpStatus.NOT_FOUND));
                    
            GioHang gioHang = new GioHang();
            gioHang.setId(new GioHangKey(maKH, maSP));
            gioHang.setKhachHang(khachHang);
            gioHang.setSanPham(sanPham);
            gioHang.setSoLuong(request.getSoLuong());
            
            gioHangRepository.save(gioHang);
        }
    }

    @Override
    @Transactional
    public void suaSoLuong(String email, CartRequestDTO request) {
        KhachHang khachHang = getKhachHangByEmail(email);
        
        GioHang gioHang = gioHangRepository.findById_MaKHAndId_MaSP(khachHang.getMaKH(), request.getMaSP())
                .orElseThrow(() -> new CustomException("Sản phẩm không có trong giỏ hàng.",HttpStatus.NOT_FOUND));
                
        Long tongTonKho = khoHangRepository.getTotalTonKhoByMaSP(request.getMaSP());
        if (request.getSoLuong() > tongTonKho) {
            throw new CustomException("Số lượng yêu cầu vượt quá tổng tồn kho hệ thống (" + tongTonKho + ").",HttpStatus.BAD_REQUEST);
        }
        
        // Ghi đè số lượng
        gioHang.setSoLuong(request.getSoLuong());
        gioHangRepository.save(gioHang);
    }

    @Override
    @Transactional
    public void xoaKhoiGioHang(String email, Long maSP) {
        KhachHang khachHang = getKhachHangByEmail(email);
        gioHangRepository.deleteById_MaKHAndId_MaSP(khachHang.getMaKH(), maSP);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponseDTO> layDanhSachGioHang(String email) {
        KhachHang khachHang = getKhachHangByEmail(email);
        List<GioHang> listGioHang = gioHangRepository.findById_MaKH(khachHang.getMaKH());
        
        return listGioHang.stream().map(gh -> {
            SanPham sp = gh.getSanPham();
            Long tongTonKho = khoHangRepository.getTotalTonKhoByMaSP(sp.getMaSP());
            
            return new CartItemResponseDTO(
                    sp.getMaSP(),
                    sp.getTenSP(),
                    sp.getLogo(),
                    sp.getGiaBan(),
                    gh.getSoLuong(),
                    tongTonKho
            );
        }).collect(Collectors.toList());
    }
}