package hcmute.controller;

import hcmute.dto.response.ProductPublicDTO;
import hcmute.dto.response.SanPhamResponse;
import hcmute.dto.response.BranchStockResponse; // BỔ SUNG IMPORT
import hcmute.service.InventoryService; // BỔ SUNG IMPORT
import hcmute.service.SanPhamService;

import java.math.BigDecimal;
import java.util.List; // BỔ SUNG IMPORT
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    // 1. BỔ SUNG INJECT INVENTORY SERVICE
    @Autowired
    private InventoryService inventoryService;

    // --- API Trang chủ ---
    @GetMapping("/")
    public String showHomePage(@RequestParam(defaultValue = "0") int page, Model model) {
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
        // Lấy thông tin chi tiết sản phẩm
        SanPhamResponse productDetail = sanPhamService.getPublicProductDetail(id);
        model.addAttribute("sanPham", productDetail);
        
        // 2. BỔ SUNG: Lấy danh sách tồn kho theo chi nhánh và đẩy ra View
        List<BranchStockResponse> branchStocks = inventoryService.checkStockByProduct(id);
        model.addAttribute("branchStocks", branchStocks);
        
        // Trả về view chi tiết sản phẩm
        return "client/product-detail";
    }
    
    // --- API Trang Cửa hàng (Tìm kiếm, Lọc, Phân trang) ---
    @GetMapping("/shop")
    public String showShopPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, 9);
        Page<ProductPublicDTO> pageSanPham = sanPhamService.getPublicProducts(keyword, null, maxPrice, null, null, pageRequest);

        model.addAttribute("listSanPham", pageSanPham.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageSanPham.getTotalPages());
        
        model.addAttribute("keyword", keyword);
        model.addAttribute("maxPrice", maxPrice);

        return "client/shop";
    }
}