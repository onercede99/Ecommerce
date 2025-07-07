package com.codegym.controller;

import com.codegym.dto.PasswordChangeDto;
import com.codegym.model.Order;
import com.codegym.model.User;
import com.codegym.repository.OrderRepository;
import com.codegym.repository.UserRepository;
import com.codegym.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;

    public AccountController(UserRepository userRepository, OrderRepository orderRepository, UserService userService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    @GetMapping("/orders")
    public String getOrderHistory(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElse(null);

        List<Order> orders = user != null ? orderRepository.findByUser_IdOrderByOrderDateDesc(user.getId()) : Collections.emptyList();

        model.addAttribute("orders", orders);
        return "account/orders";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        model.addAttribute("passwordChangeDto", new PasswordChangeDto());
        return "account/profile";
    }

    @PostMapping("/change-password")
    public String handleChangePassword(@Valid @ModelAttribute("passwordChangeDto") PasswordChangeDto passwordChangeDto,
                                       BindingResult result,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordChangeDto", result);
            redirectAttributes.addFlashAttribute("passwordChangeDto", passwordChangeDto);
            return "redirect:/account/profile";
        }

        try {
            userService.changePassword(principal.getName(), passwordChangeDto);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account/profile";
        }

        return "redirect:/account/profile";
    }
}