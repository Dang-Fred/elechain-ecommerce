package hcmute.controller;

import org.springframework.stereotype.Controller;
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

    // 2. Trả về giao diện Dashboard sau khi đăng nhập thành công
    @GetMapping({"", "/", "/dashboard"})
    public String showDashboard() {
        // Đường dẫn file Thymeleaf: src/main/resources/templates/admin/index.html
        return "admin/index";
    }

    // 3. Trả về giao diện quản lý đơn hàng
    @GetMapping("/orders")
    public String showOrdersPage() {
        return "admin/orders";
    }
}