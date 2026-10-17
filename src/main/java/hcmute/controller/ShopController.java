package hcmute.controller;

import hcmute.dto.response.ProductPublicDTO;
import hcmute.dto.response.SanPhamResponse;
import hcmute.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ShopController {

    @Autowired
    private SanPhamService sanPhamService;

 // --- API Trang chủ ---
    // Bổ sung @RequestParam để bắt tham số "?page=" trên URL (Mặc định là 0 - trang đầu tiên)
    @GetMapping("/")
    public String showHomePage(@RequestParam(defaultValue = "0") int page, Model model) {
        
        // 1. Khởi tạo PageRequest (Trang hiện tại: page, Kích thước: 1 sản phẩm/trang để test)
        PageRequest pageRequest = PageRequest.of(page, 1);
        
        // 2. Gọi API lấy dữ liệu công khai
        Page<ProductPublicDTO> productPage = sanPhamService.getPublicProducts(pageRequest, null, null);
        
        // 3. Đính kèm danh sách sản phẩm của trang hiện tại
        model.addAttribute("listSanPham", productPage.getContent());
        
        // 4. BỔ SUNG: Đính kèm thông tin phân trang để HTML vẽ nút bấm
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages()); // Trả về 2 (vì có 2 sản phẩm chia cho size 1)
        
        return "client/index"; 
    }
    
 // --- API Trang chi tiết sản phẩm ---
    @GetMapping("/product/{id}")
    public String showProductDetail(@PathVariable("id") Long id, Model model) {
        // 1. Gọi Service để lấy thông tin chi tiết sản phẩm theo ID (trả về SanPhamResponse)
        SanPhamResponse productDetail = sanPhamService.getPublicProductDetail(id);
        
        // 2. Đưa đối tượng nhận được vào Model
        model.addAttribute("sanPham", productDetail);
        
        // 3. Trả về view chi tiết sản phẩm
        return "client/product-detail";
    }
}