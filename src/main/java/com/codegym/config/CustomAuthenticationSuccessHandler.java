package com.codegym.config;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Ưu tiên chuyển hướng cho vai trò có quyền cao hơn trước
        String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            System.out.println("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Nếu người dùng có vai trò ADMIN, luôn chuyển đến dashboard
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "/admin/dashboard";
        }

        // Nếu không phải ADMIN, nhưng có vai trò STAFF, chuyển đến trang products
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))) {
            return "/admin/products"; // Hoặc "/admin/orders" tùy bạn chọn
        }

        // Nếu là người dùng thông thường (USER)
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            return "/";
        }

        // Trường hợp mặc định (dù hiếm khi xảy ra)
        return "/";
    }
}