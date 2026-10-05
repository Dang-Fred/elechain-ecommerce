// File: SecurityConfig.java
package hcmute.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Bean dùng để băm mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean cấu hình luồng bảo mật (Filter Chain)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Tắt CSRF (Bảo vệ API tĩnh)
            .csrf(csrf -> csrf.disable())
            
            // 2. Kích hoạt CORS (Dùng Bean corsConfigurationSource bên dưới)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 3. Quản lý Session: Tắt hoàn toàn Session của Spring, chuyển sang Stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 4. Phân quyền Request
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Mở cửa toàn bộ cho luồng Đăng nhập/Đăng ký
                .anyRequest().authenticated()                // Các endpoint còn lại bắt buộc phải có xác thực
            );
            
        return http.build();
    }
    
 // Cấu hình CORS cho phép mọi Domain, mọi Header và mọi Method (GET, POST, PUT, DELETE, v.v.)
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Dùng allowedOriginPatterns("*") thay vì allowedOrigins("*") để tránh xung đột với allowCredentials trong Spring Boot 3
        configuration.setAllowedOriginPatterns(List.of("*")); 
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Cho phép gửi thông tin xác thực nếu cần
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Áp dụng cho mọi endpoint
        return source;
    }
    
    
    
    
    
    
    
    
    
    
    
    
}