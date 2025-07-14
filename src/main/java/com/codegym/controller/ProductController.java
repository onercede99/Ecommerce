package com.codegym.controller;

import com.codegym.dto.ReviewDto;
import com.codegym.dto.ReviewSummaryDto;
import com.codegym.model.*;
import com.codegym.repository.BrandRepository;
import com.codegym.repository.CategoryRepository;
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

import java.util.Arrays;
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
    public String homePage(Model model) {
        Pageable pageable = PageRequest.of(0, 8, Sort.by("id").descending());
        List<Product> products = productService.findAll(null, null, null, null, null, pageable).getContent();
        model.addAttribute("products", products);
        return "home";
    }


    @GetMapping("/products")
    public String listProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "12") int size,
                               @RequestParam(name = "keyword", required = false) String keyword,
                               @RequestParam(name = "categoryId", required = false) Long categoryId,
                               @RequestParam(name = "sort", defaultValue = "latest") String sortOption,
                               @RequestParam(name = "brandId", required = false) Long brandId,
                               @RequestParam(name = "minPrice", required = false) Double minPrice,
                               @RequestParam(name = "maxPrice", required = false) Double maxPrice,
                               @RequestParam(name = "roast", required = false) String roastLevel,
                               Model model) {


        Sort sort = switch (sortOption) {
            case "price,asc" -> Sort.by("price").ascending();
            case "price,desc" -> Sort.by("price").descending();
            default -> Sort.by("id").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productService.findAll(keyword, categoryId, brandId, minPrice, maxPrice, pageable);

        List<Category> categories = categoryRepository.findAll();
        List<Brand> brands = brandRepository.findAll();
        List<RoastLevel> roastLevels = Arrays.asList(RoastLevel.values());

        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        model.addAttribute("roastLevels", roastLevels);
        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("sortOption", sortOption);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedRoastLevel", roastLevel);


        return "product/list";

    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable("id") Long id,
                                @RequestParam(name = "reviewPage", defaultValue = "0") int reviewPageNum,
                                Model model) {

        Optional<Product> productOptional = productService.findByIdWithDetails(id);
        if (productOptional.isEmpty()) {
            return "error/404";
        }
        Product product = productOptional.get();
        Pageable reviewPageable = PageRequest.of(reviewPageNum, 5);
        Page<Review> reviewPage = reviewRepository.findByProductIdWithUser(id, reviewPageable);
        ReviewSummaryDto summary = reviewRepository.getReviewSummary(id);
        List<Product> relatedProducts = productService.findRelatedProducts(product, 4);


        model.addAttribute("product", product);
        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("summary", summary);
        model.addAttribute("relatedProducts", relatedProducts);


        if (!model.containsAttribute("newReview")) {
            model.addAttribute("newReview", new ReviewDto());
        }


        return "product/detail";
    }

    @GetMapping("/test-error")
    public String testError() {
        throw new RuntimeException("This is a test error");
    }
}