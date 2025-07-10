package com.codegym.controller;

import com.codegym.dto.PasswordChangeDto;
import com.codegym.model.Order;
import com.codegym.model.User;
import com.codegym.repository.UserRepository;
import com.codegym.service.IOrderService;
import com.codegym.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/account") // Tất cả các URL sẽ bắt đầu bằng /account
public class AccountController {

    private final UserRepository userRepository;
    private final IOrderService orderService;
    private final UserService userService;

    // Sửa constructor
    public AccountController(UserRepository userRepository, IOrderService orderService, UserService userService) {
        this.userRepository = userRepository;
        this.orderService = orderService;
        this.userService = userService;
    }

    // Phương thức helper để lấy người dùng hiện tại
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // Xử lý trang chính /account
    @GetMapping
    public String viewAccountProfile(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login"; // Nếu chưa đăng nhập, chuyển về trang login
        }
        model.addAttribute("user", currentUser);
        return "account/profile"; // Trả về view account/profile.html
    }

    // Xử lý trang /account/orders
    @GetMapping("/orders")
    public String viewAccountOrders(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) { return "redirect:/login"; }

        // Lấy danh sách đơn hàng của người dùng này
        List<Order> orders = orderService.findByUser(currentUser);
        model.addAttribute("orders", orders);

        return "account/orders"; // Trả về view account/orders.html
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("passwordChangeDto", new PasswordChangeDto());

        return "account/change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@Valid @ModelAttribute("passwordChangeDto") PasswordChangeDto dto,
                                        BindingResult result,
                                        RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) { return "redirect:/login"; }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.passwordChangeDto", "Xác nhận mật khẩu không khớp.");
        }

        if (result.hasErrors()) {
            return "account/change-password";
        }

        try {
            userService.changePassword(currentUser.getUsername(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/account/change-password";
    }
}