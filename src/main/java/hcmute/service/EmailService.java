// File: EmailService.java
package hcmute.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã Xác Nhận Đăng Ký Tài Khoản - CuoiKyCNPM");
        message.setText("Xin chào,\n\n"
                + "Mã OTP để xác nhận đăng ký tài khoản của bạn là: " + otpCode + "\n"
                + "Mã này sẽ hết hạn trong vòng 60 giây.\n\n"
                + "Trân trọng,\nĐội ngũ Elechain");
        
        mailSender.send(message);
    }
}