package com.codegym.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class CartDto {
    private Map<Long, CartItemDto> items = new HashMap<>();

    public BigDecimal getTotalPrice() {
        return items.values().stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.values().stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();
    }
}
