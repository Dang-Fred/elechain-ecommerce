// File: GioHangRestController.java
package hcmute.rest;

import hcmute.dto.request.CartRequestDTO;
import hcmute.dto.response.CartItemResponseDTO;
import hcmute.service.GioHangService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/client/gio-hang")
public class GioHangRestController {

    @Autowired
    private GioHangService gioHangService;

    // UC Lấy danh sách giỏ hàng
    @GetMapping
    public ResponseEntity<List<CartItemResponseDTO>> getGioHang(Principal principal) {
        // Principal chứa thông tin user (email) sau khi parse JWT thành công
        String email = principal.getName();
        List<CartItemResponseDTO> result = gioHangService.layDanhSachGioHang(email);
        return ResponseEntity.ok(result);
    }

    // UC08 - Thêm vào giỏ hàng
    @PostMapping
    public ResponseEntity<String> themVaoGioHang(Principal principal, @Valid @RequestBody CartRequestDTO request) {
        String email = principal.getName();
        gioHangService.themVaoGioHang(email, request);
        return ResponseEntity.ok("Thêm vào giỏ hàng thành công.");
    }

    // UC09 - Sửa số lượng (Ghi đè)
    @PutMapping
    public ResponseEntity<String> suaSoLuong(Principal principal, @Valid @RequestBody CartRequestDTO request) {
        String email = principal.getName();
        gioHangService.suaSoLuong(email, request);
        return ResponseEntity.ok("Cập nhật số lượng thành công.");
    }

    // UC10 - Xóa khỏi giỏ hàng
    @DeleteMapping("/{maSP}")
    public ResponseEntity<String> xoaKhoiGioHang(Principal principal, @PathVariable Long maSP) {
        String email = principal.getName();
        gioHangService.xoaKhoiGioHang(email, maSP);
        return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng.");
    }
}