package com.codegym.controller;

import com.codegym.model.Product;
import com.codegym.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping({"/", "/home"})
    public String homePage() {
        return "home";
    }

    // ĐẢM BẢO BẠN CÓ PHƯƠNG THỨC NÀY
    @GetMapping("/products")
    public String listProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "6") int size,
                               @RequestParam(name = "keyword", required = false) String keyword,
                               Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        Page<Product> productPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        model.addAttribute("productPage", productPage);
        return "product/list";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        return "product/detail";
    }
}