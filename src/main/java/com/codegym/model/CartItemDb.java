package com.codegym.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
public class CartItemDb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    // Quan hệ N-1 với Product
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Quan hệ N-1 với Cart
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;
}