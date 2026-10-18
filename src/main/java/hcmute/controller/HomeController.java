package hcmute.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Cổng Khách hàng
    @GetMapping("/login")
    public String showClientLogin() {
        return "auth/login-client";
    }

 
//    // Trang chủ tạm thời
//    @GetMapping("/")
//    public String showHomePage() {
//        return "redirect:/login"; 
//    }
    
 // Cổng Đăng ký
    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register";
    }
}