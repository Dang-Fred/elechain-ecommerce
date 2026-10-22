package hcmute.controller;

import hcmute.dto.response.ProductPublicDTO;
import hcmute.dto.response.SanPhamResponse;
import hcmute.service.SanPhamService;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
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
        
        // ĐỔI 6 THÀNH 8: Để có đủ dữ liệu lấp đầy 2 dòng x 4 cột
        PageRequest pageRequest = PageRequest.of(page, 8);
        
        Page<ProductPublicDTO> productPage = sanPhamService.getPublicProducts(pageRequest, null, null);
        
        model.addAttribute("listSanPham", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        
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
    
 // --- API Trang Cửa hàng (Tìm kiếm, Lọc, Phân trang) ---
    @GetMapping("/shop")
    public String showShopPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // 1. Khởi tạo PageRequest lấy 9 sản phẩm 1 trang
        PageRequest pageRequest = PageRequest.of(page, 9);

        // 2. Gọi service (truyền null cho minPrice, maDM và maHang theo đúng thứ tự tham số)
        Page<ProductPublicDTO> pageSanPham = sanPhamService.getPublicProducts(keyword, null, maxPrice, null, null, pageRequest);

        // 3. Đẩy vào Model để hiển thị trên giao diện
        model.addAttribute("listSanPham", pageSanPham.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageSanPham.getTotalPages());
        
        // 4. Giữ trạng thái của bộ lọc trên giao diện
        model.addAttribute("keyword", keyword);
        model.addAttribute("maxPrice", maxPrice);

        return "client/shop";
    }
}