package com.codegym.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class CartDto {
    private Map<Long, CartItemDto> items = new HashMap<>();
    private int totalItems;
    private BigDecimal totalPrice;

}
