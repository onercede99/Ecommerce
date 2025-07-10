// File: src/main/java/com/codegym/controller/ProductController.java
package com.codegym.controller;

import com.codegym.dto.ReviewSummaryDto;
import com.codegym.model.Brand;
import com.codegym.model.Category;
import com.codegym.model.Product;
import com.codegym.model.Review;
import com.codegym.repository.BrandRepository;
import com.codegym.repository.CategoryRepository; // Vẫn giữ lại để lấy danh sách category
import com.codegym.repository.ReviewRepository;
import com.codegym.service.IProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {
    private final IProductService productService;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ReviewRepository reviewRepository;

    public ProductController(IProductService productService, CategoryRepository categoryRepository, BrandRepository brandRepository, ReviewRepository reviewRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping({"/", "/home"})
    public String homePage() {
        return "home";
    }

    // =========================================================================
    // ĐÂY LÀ PHƯƠNG THỨC ĐÃ ĐƯỢC ĐƠN GIẢN HÓA HOÀN TOÀN
    // =========================================================================
    @GetMapping("/products")
    public String listProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "12") int size, // Tăng size lên 12 cho đẹp
                               @RequestParam(name = "keyword", required = false) String keyword,
                               @RequestParam(name = "categoryId", required = false) Long categoryId,
                               @RequestParam(name = "sort", defaultValue = "latest") String sortOption,
                               @RequestParam(name = "brandId", required = false) Long brandId,
                               @RequestParam(name = "minPrice", required = false) Double minPrice,
                               @RequestParam(name = "maxPrice", required = false) Double maxPrice,
                               Model model) {

        // 1. Tạo đối tượng Sort từ tham số sortOption
        Sort sort = switch (sortOption) {
            case "price,asc" -> Sort.by("price").ascending();
            case "price,desc" -> Sort.by("price").descending();
            default -> Sort.by("id").descending(); // Sắp xếp theo ID mới nhất
        };

        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. GỌI MỘT PHƯƠNG THỨC DUY NHẤT TỪ SERVICE
        //    Service sẽ tự xử lý tất cả các logic lọc bên trong.
        Page<Product> productPage = productService.findAll(keyword, categoryId,brandId, minPrice, maxPrice, pageable);

        // 3. Lấy danh sách categories để hiển thị sidebar
        List<Category> categories = categoryRepository.findAll();
        List<Brand> brands = brandRepository.findAll();

        // 4. Đưa tất cả dữ liệu cần thiết vào Model để View sử dụng
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sortOption", sortOption);
        model.addAttribute("brandId", brandId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "product/list";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable("id") Long id,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                Model model) {
        // Tìm sản phẩm
        Optional<Product> productOptional = productService.findByIdWithDetails(id);
        if (productOptional.isEmpty()) {
            return "error/404";
        }

        Product product = productOptional.get();

        Pageable reviewPageable = PageRequest.of(page, 5);
        Page<Review> reviewPage = reviewRepository.findByProductIdOrderByReviewDateDesc(id, reviewPageable);
        ReviewSummaryDto summary = reviewRepository.getReviewSummary(id);
        List<Product> relatedProducts = productService.findRelatedProducts(product, 4);

        model.addAttribute("product", productOptional.get());
        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("summary", summary);
        model.addAttribute("relatedProducts", relatedProducts);

        return "product/detail";
    }

    @GetMapping("/test-error")
    public String testError() {
        throw new RuntimeException("This is a test error");
    }
}