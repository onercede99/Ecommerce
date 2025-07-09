package com.codegym.controller;

import com.codegym.model.Category;
import com.codegym.repository.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;

    public AdminCategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // 1. Hiển thị danh sách tất cả category
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAllWithProducts());
        return "admin/category/list";
    }

    // 2. Hiển thị form để thêm mới
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Add New Category");
        return "admin/category/form";
    }

    // 3. Hiển thị form để chỉnh sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
            model.addAttribute("category", category);
            model.addAttribute("pageTitle", "Edit Category (ID: " + id + ")");
            return "admin/category/form";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    // 4. Xử lý việc lưu (cả thêm mới và cập nhật)
    @PostMapping("/save")
    public String saveCategory(@Valid @ModelAttribute("category") Category category,
                               BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", category.getId() == null ? "Add New Category" : "Edit Category");
            return "admin/category/form";
        }

        try {
            categoryRepository.save(category);
            ra.addFlashAttribute("message", "The category has been saved successfully.");
        } catch (Exception e) {
            // Bắt lỗi nếu tên category bị trùng (do có ràng buộc unique trong CSDL)
            ra.addFlashAttribute("errorMessage", "Could not save the category. It might be because the name already exists.");
        }

        return "redirect:/admin/categories";
    }

    // 5. Xử lý việc xóa
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));

            if (!category.getProducts().isEmpty()) {
                throw new IllegalStateException("Cannot delete category '" + category.getName() + "' because it contains products.");
            }

            categoryRepository.deleteById(id);
            ra.addFlashAttribute("message", "The category with id " + id + " has been deleted.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}