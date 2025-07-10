package com.codegym.service;

import com.codegym.model.Category;
import com.codegym.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IProductService {
    Page<Product> findAll(String keyword, Long categoryId, Long brandId, Double minPrice, Double maxPrice, Pageable pageable);
    Optional<Product> findById(Long id);
    Optional<Product> findByIdWithDetails(Long id);
    List<Product> findRelatedProducts(Product product, int limit);


}