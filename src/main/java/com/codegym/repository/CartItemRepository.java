package com.codegym.repository;

import com.codegym.model.CartItemDb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface CartItemRepository extends JpaRepository<CartItemDb, Long> {
    @Modifying
    @Transactional
    void deleteByCartId(Long cartId);

}