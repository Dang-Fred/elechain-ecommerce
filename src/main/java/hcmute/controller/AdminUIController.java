package hcmute.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminUIController {

    // 1. Trả về giao diện đăng nhập
    @GetMapping("/login")
    public String showLoginPage() {
        // Đường dẫn file Thymeleaf: src/main/resources/templates/admin/login.html
        return "auth/login-admin"; 
    }
    
    
    @GetMapping({"", "/", "/dashboard"})
    public String showDashboard(Model model) {
        // 1. Lấy thông tin xác thực hiện tại từ Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 2. Trích xuất Role (Ví dụ: "ROLE_ADMIN", "ROLE_QLCN"...)
        String userRole = "";
        if (authentication != null && authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            // Lấy role đầu tiên trong danh sách quyền
            userRole = authentication.getAuthorities().iterator().next().getAuthority();
        }
     // Nếu là Nhân viên bán hàng, đẩy thẳng sang trang Quản lý đơn hàng
        if ("NV_BANHANG".equals(userRole)) {
            return "redirect:/admin/orders";
        }
        // (Tương tự, nếu là NV_KHO thì đẩy sang trang quản lý kho)
        if ("NV_KHO".equals(userRole)) {
            return "redirect:/admin/products"; // Hoặc URL trang kho của anh
        }
        // ----------------------------------
        
        // Nếu là ADMIN hoặc QLCN thì mới cho vào xem Dashboard
        model.addAttribute("role", userRole);
        return "admin/index";
    }

   
    @GetMapping("/orders")
    public String showOrdersPage(Model model) {
        // LƯU Ý: Ở các trang con, anh cũng cần truyền role xuống model
        // để Sidebar bên trong layout-admin.html có thể đọc được biến ${role}
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = (auth != null && !auth.getAuthorities().isEmpty()) 
                      ? auth.getAuthorities().iterator().next().getAuthority() : "";
        model.addAttribute("role", role);
        
        return "admin/orders";
    }
    
    
    @GetMapping("/products")
    public String showProductsPage(Model model) {
        // Lấy thông tin xác thực hiện tại từ Security Context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Trích xuất Role để truyền xuống giao diện (Phân quyền nút Thêm/Sửa/Xóa)
        String role = (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) 
                      ? auth.getAuthorities().iterator().next().getAuthority() : "";
                      
        model.addAttribute("role", role);
        
        // Trả về file templates/admin/products.html
        return "admin/products";
    }
}