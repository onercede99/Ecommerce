package com.codegym.controller;

import com.codegym.model.Role;
import com.codegym.model.User;
import com.codegym.repository.RoleRepository;
import com.codegym.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminUserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    // 1. Hiển thị danh sách người dùng
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/user/list";
    }

    // 2. Hiển thị form chỉnh sửa vai trò cho một người dùng
    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
            List<Role> allRoles = roleRepository.findAll();

            model.addAttribute("user", user);
            model.addAttribute("allRoles", allRoles); // Gửi tất cả các role có thể có
            return "admin/user/edit";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    // 3. Xử lý việc cập nhật vai trò
    @PostMapping("/edit")
    public String updateUserRoles(@RequestParam Long userId,
                                  @RequestParam(name = "roles", required = false) List<Long> roleIds,
                                  RedirectAttributes ra) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userId));

            // Chuyển danh sách các ID của role thành một Set<Role>
            Set<Role> newRoles = (roleIds == null) ?
                    Set.of() :
                    roleRepository.findAllById(roleIds).stream().collect(Collectors.toSet());

            user.setRoles(newRoles);
            userRepository.save(user);

            ra.addFlashAttribute("message", "User '" + user.getUsername() + "' roles have been updated successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}