package com.codegym.repository;

import com.codegym.model.CartItemDb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItemDb, Long> {
}