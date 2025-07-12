package com.codegym.dto;

import com.codegym.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private ProductInCartDto product;
    private int quantity;
    private BigDecimal subtotal;


}