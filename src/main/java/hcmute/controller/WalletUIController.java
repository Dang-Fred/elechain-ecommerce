//WalletUIController.java
package hcmute.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WalletUIController {

    @GetMapping("/wallet")
    public String showWalletPage() {
        // Chỉ trả về view, dữ liệu sẽ được load bằng Fetch API ở client
        return "client/wallet";
    }
}