package hcmute.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderUIController {

    @GetMapping("/my-orders")
    public String showMyOrdersPage() {
        // Chỉ trả về view, dữ liệu sẽ được load bằng Fetch API ở client
        return "client/my-orders";
    }
}