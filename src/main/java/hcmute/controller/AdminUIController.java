package hcmute.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminUIController {

    // Trả về trang chủ Dashboard
    @GetMapping({"", "/", "/dashboard"})
    public String showDashboard() {
        return "admin/index";
    }

    // Trả về trang Quản lý Đơn hàng
    @GetMapping("/orders")
    public String showOrdersPage() {
        // Tương tự, nếu file orders của bạn đang kế thừa layout-admin,
        // nó sẽ tự ráp vào bộ khung mới này.
        return "admin/orders";
    }
}