//ShopController.java
package hcmute.controller;

import hcmute.dto.response.SanPhamResponse;
import hcmute.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ShopController {

    @Autowired
    private SanPhamService sanPhamService;

    // --- 1. Mapping Trang chủ ---
    @GetMapping("/")
    public String showHomePage(Model model) {
        // Lấy danh sách sản phẩm thật từ Database
        // Truyền vai trò "ADMIN" và mã CN null để lấy TẤT CẢ sản phẩm đang kích hoạt
        List<SanPhamResponse> listSanPham = sanPhamService.getSanPhams("ADMIN", null);
        
        // Đưa dữ liệu sang View
        model.addAttribute("listSanPham", listSanPham);
        
        return "client/index"; // Trỏ đến file templates/client/index.html
    }

    // --- 2. Mapping Chi tiết sản phẩm ---
    @GetMapping("/product/{id}")
    public String showProductDetail(@PathVariable("id") Long id, Model model) {
        // Tạm thời lấy danh sách tổng và lọc ra 1 SP (vì Service chưa có hàm lấy chi tiết)
        // Khi nào bạn viết hàm getById() trong Service thì sẽ tối ưu lại sau
        SanPhamResponse sp = sanPhamService.getSanPhams("ADMIN", null).stream()
                .filter(s -> s.getMaSP().equals(id))
                .findFirst()
                .orElse(null);
                
        model.addAttribute("sanPham", sp);
        
        return "client/product-detail"; // File này sẽ thiết kế ở Ticket tiếp theo
    }
}