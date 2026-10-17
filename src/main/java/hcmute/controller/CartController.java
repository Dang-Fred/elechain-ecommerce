//CartController.java
package hcmute.controller;

import hcmute.dto.response.CartItemResponseDTO;
import hcmute.service.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private GioHangService gioHangService;

    @GetMapping("/cart")
    public String showCartPage(Model model, Principal principal) {
        // 1. Kiểm tra xem người dùng đã đăng nhập chưa (Bảo vệ route)
        if (principal == null) {
            return "redirect:/login"; // Yêu cầu đăng nhập nếu chưa có Principal
        }

        // 2. Lấy email thật của user đang đăng nhập từ Spring Security
        String email = principal.getName(); 

        // 3. Truy vấn dữ liệu giỏ hàng thật từ Database
        List<CartItemResponseDTO> listCart = gioHangService.layDanhSachGioHang(email);

        model.addAttribute("listCart", listCart);
        return "client/cart";
    }
    
    
}