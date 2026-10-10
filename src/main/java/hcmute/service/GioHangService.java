// File: GioHangService.java
package hcmute.service;

import hcmute.dto.request.CartRequestDTO;
import hcmute.dto.response.CartItemResponseDTO;
import java.util.List;

public interface GioHangService {
    void themVaoGioHang(String email, CartRequestDTO request);
    void suaSoLuong(String email, CartRequestDTO request);
    void xoaKhoiGioHang(String email, Long maSP);
    List<CartItemResponseDTO> layDanhSachGioHang(String email);
}