package hcmute.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF chuẩn cho Spring Boot 3
            .cors(AbstractHttpConfigurer::disable) // Tắt CORS (nếu cần test nội bộ)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Mở toang toàn bộ API để test Chat 1
            );
        return http.build();
    }
}