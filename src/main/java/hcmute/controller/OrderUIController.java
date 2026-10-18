package hcmute.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OrderUIController {

    @GetMapping("/my-orders")
    public String showMyOrdersPage() {
        // Chỉ trả về view, dữ liệu sẽ được load bằng Fetch API ở client
        return "client/my-orders";
    }
    
    @GetMapping("/my-orders/detail/{id}")
    public String showOrderDetailPage(@PathVariable("id") Long id, Model model) {
        // Trả mã đơn hàng ra view để JS đọc được
        model.addAttribute("maDH", id);
        return "client/order-detail";
    }
}