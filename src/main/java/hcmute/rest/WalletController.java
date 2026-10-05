// File: WalletController.java
package hcmute.rest;

import hcmute.dto.request.TopupRequest;
import hcmute.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        Map<String, Object> response = new HashMap<>();
        try {
        	BigDecimal balance = walletService.getBalance();
            response.put("success", true);
            response.put("message", "Lấy số dư thành công");
            response.put("data", balance);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, response);
        }
    }

    @PostMapping("/topup")
    public ResponseEntity<?> topup(@RequestBody TopupRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
        	BigDecimal newBalance = walletService.topup(request);
            response.put("success", true);
            response.put("message", "Nạp tiền thành công");
            response.put("data", newBalance);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e, response);
        }
    }

    // Hàm xử lý lỗi chung (tương tự ProfileController)
    private ResponseEntity<?> handleException(Exception e, Map<String, Object> response) {
        response.put("success", false);
        String errorMessage = e.getMessage();

        if (errorMessage != null && errorMessage.startsWith("BAD_REQUEST:")) {
            response.put("message", errorMessage.split(":")[1]);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else if (errorMessage != null && errorMessage.startsWith("UNAUTHORIZED:")) {
            response.put("message", errorMessage.split(":")[1]);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        response.put("message", errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}