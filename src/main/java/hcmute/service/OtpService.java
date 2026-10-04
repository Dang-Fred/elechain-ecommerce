// File: OtpService.java
package hcmute.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    
    // Lưu trữ OTP tạm thời. Key = email, Value = Data chứa OTP và thời gian tạo
	private final Map<String, OtpCacheInfo> otpStorage = new ConcurrentHashMap<>();
    private final long OTP_EXPIRATION_TIME = 60 * 1000; // 60 giây
    
 // Enum định nghĩa trạng thái OTP
    public enum OtpStatus {
        VALID, INVALID, EXPIRED
    }

    public String generateAndStoreOtp(String tenKH, String email, String matKhauBam) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        OtpCacheInfo cacheInfo = new OtpCacheInfo(tenKH, email, matKhauBam, otp, System.currentTimeMillis());
        otpStorage.put(email, cacheInfo);
        
        return otp;
    }
    
 // Hàm mới: Trả về trạng thái chi tiết của OTP
    public OtpStatus validateOtpWithStatus(String email, String otp) {
        OtpCacheInfo cacheInfo = otpStorage.get(email);
        
        if (cacheInfo == null) {
            return OtpStatus.EXPIRED; // Hết hạn hoặc Email không đúng
        }

        if (System.currentTimeMillis() - cacheInfo.getTimestamp() > OTP_EXPIRATION_TIME) {
            otpStorage.remove(email);
            return OtpStatus.EXPIRED;
        }

        if (cacheInfo.getOtp().equals(otp)) {
            return OtpStatus.VALID;
        } else {
            return OtpStatus.INVALID;
        }
    }
    
    
    public OtpCacheInfo getOtpCacheInfo(String email) {
        return otpStorage.get(email);
    }

    public void clearOtp(String email) {
        otpStorage.remove(email);
    }

 // Class/Record chứa toàn bộ thông tin lưu trong RAM
    @Getter
    @AllArgsConstructor
    public static class OtpCacheInfo {
        private String tenKH;
        private String email;
        private String matKhauBam;
        private String otp;
        private long timestamp;
    }
}