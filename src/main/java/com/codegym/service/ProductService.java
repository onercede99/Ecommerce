// File: src/main/java/com/codegym/service/ProductService.java
package com.codegym.service;

import com.codegym.model.Product;
import com.codegym.repository.ProductRepository;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService implements IProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Page<Product> findAll(String keyword, Long categoryId,Long brandId, Double minPrice, Double maxPrice, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);

        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("id"), categoryId));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }
        if (brandId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("brand").get("id"), brandId));
        }


        return productRepository.findAll(spec, pageable);
    }

    @Transactional // QUAN TRỌNG: Cần Transactional để load lazy data
    public Optional<Product> findById(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        productOpt.ifPresent(product -> {
            // "Chạm" vào các collection để Hibernate tải chúng
            Hibernate.initialize(product.getImages());
        });
        return productOpt;
    }

    @Override
    @Transactional
    public Optional<Product> findByIdWithDetails(Long id) {
        return productRepository.findByIdWithDetails(id);
    }

    @Override
    public List<Product> findRelatedProducts(Product product, int limit) {
        if (product == null || product.getCategory() == null || product.getId() == null) {
            return new ArrayList<>();
        }
        Pageable pageable = PageRequest.of(0, limit);

        // Gọi phương thức trong repository
        return productRepository.findByCategoryAndIdNot(product.getCategory(), product.getId(), pageable);
    }
}