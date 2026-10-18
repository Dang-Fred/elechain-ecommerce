// File: JwtAuthenticationFilter.java
package hcmute.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie; // BẮT BUỘC IMPORT THÊM CÁI NÀY
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import hcmute.util.JwtUtil;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String jwtToken = null;

        // 1. Thử lấy Token từ Header (Phục vụ cho các lệnh Fetch API từ file common.js)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        } 
        // 2. NẾU HEADER KHÔNG CÓ, TÌM TRONG COOKIE (Phục vụ cho Thymeleaf khi gõ URL trên trình duyệt)
        else if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt_token".equals(cookie.getName())) { // Tên cookie được tạo lúc đăng nhập thành công
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }

        // 3. Nếu cả Header và Cookie đều KHÔNG có Token -> Trả về khách vãng lai, cho đi tiếp (Sẽ bị SecurityConfig chặn nếu api yêu cầu quyền)
        if (jwtToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Đã tìm thấy Token (từ Header hoặc Cookie), tiến hành giải mã
        String userEmail = jwtUtil.extractEmail(jwtToken);

        // 5. Nếu có email và chưa được xác thực trong SecurityContext
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            if (jwtUtil.validateToken(jwtToken)) {
                // Đọc Role động từ Token thay vì fix cứng
                String role = jwtUtil.extractRole(jwtToken); 
                
                // Spring Security chuẩn thường yêu cầu prefix "ROLE_"
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail, null, Collections.singletonList(authority)
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
   
    
    
    
}