// File: SecurityConfig.java
package hcmute.config;
import hcmute.security.JwtAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity

public class SecurityConfig {

    // Bean dùng để băm mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
                // ==========================================
                // NHÓM 1: MỞ CỬA TÀI NGUYÊN TĨNH (Static Resources)
                // ==========================================
                .requestMatchers("/css/**", "/js/**", "/images/**", "/*.css", "/*.js", "/fonts/**", "/favicon.ico", "/electro/**").permitAll()

                // ==========================================
                // NHÓM 2: MỞ CỬA CÁC TRANG GIAO DIỆN (Frontend Views & Error)
                // ==========================================
                .requestMatchers("/", "/login", "/admin/login", "/register", "/error").permitAll()
                .requestMatchers("/product/**", "/shop/**").permitAll() 
                .requestMatchers("/admin/**").permitAll() // Lưu ý: Đang mở cửa toàn bộ UI Admin (Nếu bảo mật UI bằng Thymeleaf/Interceptor thì để nguyên)

                // ==========================================
                // NHÓM 3: MỞ CỬA CÁC API PUBLIC (Không cần Token)
                // ==========================================
                .requestMatchers("/api/auth/**", "/api/public/**", "/api/admin/login").permitAll() // Luồng đăng nhập, đăng ký
                .requestMatchers(HttpMethod.GET, "/api/reviews/product/**").permitAll() // CHỈ CHO request method GET của API Đánh giá
                .requestMatchers("/api/inventory/check/**").permitAll() // API tra cứu tồn kho public cho khách

                // ==========================================
                // NHÓM 4: PHÂN QUYỀN API THEO ROLE (Role-based)
                // ==========================================
                // Cấp quyền GET Sản phẩm cho NV_KHO, QLCN và ADMIN để phục vụ lập Phiếu Nhập
                .requestMatchers(HttpMethod.GET, "/api/v1/san-pham", "/api/v1/san-pham/**").hasAnyAuthority("ADMIN", "QLCN", "NV_KHO")

                // ==========================================
                // NHÓM 5: CHẶN TẤT CẢ CÁC REQUEST CÒN LẠI
                // ==========================================
                .anyRequest().authenticated()
                         
            )
            // Lắp filter của chúng ta vào
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
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