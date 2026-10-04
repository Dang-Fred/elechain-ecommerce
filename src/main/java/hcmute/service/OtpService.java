// File: OtpService.java
package hcmute.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    
    // Lưu trữ OTP tạm thời. Key = email, Value = Data chứa OTP và thời gian tạo
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final long OTP_EXPIRATION_TIME = 60 * 1000; // 60 giây

    public String generateAndStoreOtp(String email) {
        // Tạo OTP 6 số ngẫu nhiên
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        // Lưu vào Map
        otpStorage.put(email, new OtpData(otp, System.currentTimeMillis()));
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        OtpData otpData = otpStorage.get(email);
        
        if (otpData == null) {
            return false; // Không tồn tại hoặc đã bị xóa
        }

        // Kiểm tra thời gian hết hạn (60s)
        long currentTime = System.currentTimeMillis();
        if (currentTime - otpData.getTimestamp() > OTP_EXPIRATION_TIME) {
            otpStorage.remove(email); // Xóa OTP hết hạn để giải phóng bộ nhớ
            return false;
        }

        // Kiểm tra tính hợp lệ
        boolean isValid = otpData.getOtp().equals(otp);
        if (isValid) {
            // Xác nhận thành công thì xóa đi luôn (One-Time Password)
            otpStorage.remove(email);
        }
        return isValid;
    }

    // Lớp nội bộ để giữ OTP và Timestamp
    private static class OtpData {
        private final String otp;
        private final long timestamp;

        public OtpData(String otp, long timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }

        public String getOtp() { return otp; }
        public long getTimestamp() { return timestamp; }
    }
}