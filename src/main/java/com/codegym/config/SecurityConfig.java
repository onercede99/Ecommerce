package com.codegym.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true);
        return firewall;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // .csrf().disable() // Giữ nguyên, không thay đổi
                .authorizeRequests(authorize -> authorize
                        // ===== SỬA ĐỔI QUAN TRỌNG: ĐƯA API CART VÀO QUY TẮC RIÊNG =====
                        .antMatchers("/register/**", "/", "/home", "/products/**", "/login").permitAll() // Tách cart và checkout ra khỏi đây
                        .antMatchers("/cart/**", "/checkout", "/order/place").permitAll() // Cho phép truy cập trang cart, checkout
                        .antMatchers("/reviews/add/**").authenticated() // Yêu cầu đăng nhập để review
                        .antMatchers("/api/cart/**").authenticated() // YÊU CẦU ĐĂNG NHẬP ĐỂ GỌI API CART
                        .antMatchers("/css/**", "/js/**", "/product-images/**", "/images/**").permitAll()
                        .antMatchers("/admin/users/**", "/admin/dashboard").hasRole("ADMIN")
                        .antMatchers("/admin/api/**").hasRole("ADMIN")
                        .antMatchers("/admin/products/**", "/admin/orders/**", "/admin/home").hasAnyRole("ADMIN", "STAFF")
                        .antMatchers("/account/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthenticationSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // ===== THÊM PHẦN XỬ LÝ LỖI AJAX VÀO CUỐI =====
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new AjaxAwareAuthenticationEntryPoint("/login"))
                );

        return http.build();
    }


    // ===== THÊM LỚP HELPER NÀY VÀO CUỐI FILE, BÊN NGOÀI CLASS SecurityConfig =====
    class AjaxAwareAuthenticationEntryPoint implements AuthenticationEntryPoint {
        private final String loginUrl;

        public AjaxAwareAuthenticationEntryPoint(String loginUrl) {
            this.loginUrl = loginUrl;
        }

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

            if (isAjax) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please log in.");
            } else {
                response.sendRedirect(request.getContextPath() + loginUrl);
            }
        }
    }
}