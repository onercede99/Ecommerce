package com.codegym.controller;

import com.codegym.model.Product;
import com.codegym.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminProductController.class);


    private final ProductRepository productRepository;

    public AdminProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public String listProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "5") int size,
                               @RequestParam(name = "keyword", required = false) String keyword,
                               Model model) {

        // Tạo đối tượng Pageable để yêu cầu phân trang và sắp xếp
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Product> productPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // Nếu có từ khóa, tìm kiếm và phân trang
            productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            // Nếu không có, lấy tất cả và phân trang
            productPage = productRepository.findAll(pageable);
        }

        model.addAttribute("productPage", productPage);

        return "admin/product/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("pageTitle", "Add New Product");
        return "admin/product/form";
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult result,
                              @RequestParam("imageFile") MultipartFile multipartFile,
                              RedirectAttributes ra,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", product.getId() == null ? "Add New Product" : "Edit Product");
            return "admin/product/form";
        }

        try {
            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

            if (fileName != null && !fileName.isEmpty()) {
                product.setImageUrl(fileName);
            } else if (product.getId() != null) {
                Product existingProduct = productRepository.findById(product.getId()).orElse(null);
                if (existingProduct != null) {
                    product.setImageUrl(existingProduct.getImageUrl());
                }
            }

            Product savedProduct = productRepository.save(product);
            LOGGER.info("Saved product with ID: {}", savedProduct.getId());

            if (!multipartFile.isEmpty()) {
                String uploadDir = "product-images/" + savedProduct.getId();

                LOGGER.info("Attempting to save file to relative path: {}", uploadDir);
                try {
                    saveFile(uploadDir, fileName, multipartFile);
                    LOGGER.info("Successfully saved file: {}", fileName);
                } catch (IOException e) {
                    LOGGER.error("Error saving image file", e);
                    ra.addFlashAttribute("errorMessage", "Could not save the image file: " + e.getMessage());
                }
            }


            ra.addFlashAttribute("message", "The product has been saved successfully.");

        } catch (Exception e) {
            LOGGER.error("Error saving image file", e);
            ra.addFlashAttribute("errorMessage", "Could not save the image file: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }


    private void saveFile(String uploadDir, String fileName, MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            LOGGER.info("Creating directory: {}", uploadPath);
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new IOException("Could not save image file: " + fileName, ioe);
        }
    }


    // HIỂN THỊ FORM CHỈNH SỬA
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", "Edit Product (ID: " + id + ")");
            return "admin/product/form";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            if (!productRepository.existsById(id)) {
                throw new IllegalArgumentException("Product not found with id: " + id);
            }

            String sql = "SELECT COUNT(*) FROM order_details WHERE product_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);

            if (count != null && count > 0) {
                throw new IllegalStateException("Cannot delete product with id " + id + " because it is associated with existing orders.");
            }

            String uploadDir = "./product-images/" + id;
            Path uploadPath = Paths.get(uploadDir);

            if (Files.exists(uploadPath)) {
                LOGGER.info("Deleting image directory: {}", uploadPath);
                FileSystemUtils.deleteRecursively(uploadPath);
            }

            productRepository.deleteById(id);

            ra.addFlashAttribute("message", "The product with id " + id + " has been successfully deleted.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.error("Error deleting product: {}", e.getMessage());
            ra.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error deleting product with id " + id, e);
            ra.addFlashAttribute("errorMessage", "An unexpected error occurred while deleting the product.");
        }

        return "redirect:/admin/products";
    }


}