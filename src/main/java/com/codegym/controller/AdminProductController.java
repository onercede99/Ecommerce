package com.codegym.controller;

import com.codegym.model.Product;
import com.codegym.repository.CategoryRepository;
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
import java.util.Date;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminProductController.class);


    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public AdminProductController(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
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
        // Gửi danh sách tất cả category ra view
        model.addAttribute("allCategories", categoryRepository.findAll());
        model.addAttribute("pageTitle", "Add New Product");
        return "admin/product/form";
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult result,
                              @RequestParam("imageFile") MultipartFile mainImageFile,
                              @RequestParam(name = "extraImageFiles", required = false) MultipartFile[] extraImageFiles,
                              RedirectAttributes ra,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", product.getId() == null ? "Add New Product" : "Edit Product");
            model.addAttribute("allCategories", categoryRepository.findAll()); // Thêm lại để form không bị lỗi
            return "admin/product/form";
        }

        try {
            // ---- XỬ LÝ ẢNH CHÍNH ----
            if (!mainImageFile.isEmpty()) {
                String originalFileName = StringUtils.cleanPath(mainImageFile.getOriginalFilename());
                String sanitizedFileName = new Date().getTime() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
                product.setImageUrl(sanitizedFileName);
            } else {
                if (product.getId() != null) { // Trường hợp edit mà không thay ảnh
                    productRepository.findById(product.getId()).ifPresent(existingProd -> product.setImageUrl(existingProd.getImageUrl()));
                }
            }

            // ---- LƯU SẢN PHẨM VÀO DB ĐỂ LẤY ID ----
            // Quan trọng: Phải lưu trước để có ID, dùng ID này tạo thư mục
            Product savedProduct = productRepository.save(product);
            LOGGER.info("Saved/Updated Product with ID: {}", savedProduct.getId());

            // ---- TẠO ĐƯỜNG DẪN LƯU TRỮ ----
            Path projectRootPath = Paths.get("").toAbsolutePath();
            Path uploadPath = projectRootPath.resolve("product-images").resolve(String.valueOf(savedProduct.getId()));

            // ---- LƯU FILE ẢNH CHÍNH ----
            if (!mainImageFile.isEmpty()) {
                saveFile(uploadPath, product.getImageUrl(), mainImageFile);
            }

            // ---- XỬ LÝ VÀ LƯU CÁC FILE ẢNH PHỤ (LOGIC ĐÚNG) ----
            Path extrasUploadPath = uploadPath.resolve("extras");
            for (MultipartFile extraFile : extraImageFiles) {
                if (!extraFile.isEmpty()) {
                    // 1. Tạo tên file an toàn cho ảnh phụ
                    String originalFileName = StringUtils.cleanPath(extraFile.getOriginalFilename());
                    String sanitizedFileName = new Date().getTime() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9.-]", "_");

                    // 2. Thêm ảnh phụ vào đối tượng Product (để lưu vào DB)
                    // Ghi chú: Cần phải lưu lại product sau khi thêm ảnh phụ
                    savedProduct.addExtraImage(sanitizedFileName);

                    // 3. Lưu file vật lý vào thư mục extras
                    saveFile(extrasUploadPath, sanitizedFileName, extraFile);
                }
            }

            // Sau khi thêm các ảnh phụ, lưu lại product một lần nữa để cập nhật danh sách ảnh phụ
            productRepository.save(savedProduct);


            ra.addFlashAttribute("message", "The product has been saved successfully.");

        } catch (IOException e) {
            LOGGER.error("Error during file saving process", e);
            ra.addFlashAttribute("errorMessage", "Could not save image file. Please check server logs for details.");
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred during product save", e);
            ra.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }





    private void saveFile(Path uploadPath, String fileName, MultipartFile multipartFile) throws IOException {
        // Paths.get(uploadDir) sẽ tạo ra một đường dẫn tương đối so với thư mục gốc của project
//        Path uploadPath = Paths.get(uploadDir);

        // Tạo thư mục nếu nó chưa tồn tại
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            LOGGER.info("Created directory: {}", uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Could not save image file: " + fileName, e);
        }
    }


    // HIỂN THỊ FORM CHỈNH SỬA
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
            model.addAttribute("product", product);
            // Gửi danh sách tất cả category ra view
            model.addAttribute("allCategories", categoryRepository.findAll());
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