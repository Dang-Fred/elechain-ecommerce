//CheckoutController.java
package hcmute.controller;

import hcmute.dto.response.CartItemResponseDTO;
import hcmute.dto.response.ChiNhanhResponse; 
import hcmute.entity.KhachHang;
import hcmute.repository.KhachHangRepository; // Gọi trực tiếp Repository
import hcmute.service.ChiNhanhService;
import hcmute.service.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class CheckoutController {

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private KhachHangRepository khachHangRepository; // Tận dụng kho chứa có sẵn

    @Autowired
    private ChiNhanhService chiNhanhService;

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, Principal principal) {
        // 1. Chặn người dùng chưa đăng nhập
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();

        // 2. Lấy giỏ hàng
        List<CartItemResponseDTO> listCart = gioHangService.layDanhSachGioHang(email);
        if (listCart == null || listCart.isEmpty()) {
            return "redirect:/cart";
        }

        // 3. Lấy thông tin Khách Hàng bằng Repository (Y hệt cách ProfileService làm)
        KhachHang khachHang = khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));

        // 4. Lấy danh sách Chi Nhánh đang hoạt động
        List<ChiNhanhResponse> listChiNhanh = chiNhanhService.getAllActive();

        // 5. Đẩy dữ liệu ra giao diện Thymeleaf
        model.addAttribute("listCart", listCart);
        model.addAttribute("khachHang", khachHang);
        model.addAttribute("listChiNhanh", listChiNhanh);

        return "client/checkout";
    }
}