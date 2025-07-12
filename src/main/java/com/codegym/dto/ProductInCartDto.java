package com.codegym.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductInCartDto {
    private Long id;
    private String name;
    private String photosImagePath;
    private BigDecimal price;
}