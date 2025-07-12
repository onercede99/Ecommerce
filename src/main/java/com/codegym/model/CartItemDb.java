package com.codegym.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
public class CartItemDb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY) // Dùng LAZY để tối ưu
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY) // Dùng LAZY để tối ưu
    @JoinColumn(name = "cart_id")
    private Cart cart;

    // PHƯƠNG THỨC TÍNH TOÁN QUAN TRỌNG
    // @Transient nói cho JPA biết đây không phải là một cột trong DB
    @Transient
    public BigDecimal getSubtotal() {
        if (product != null && product.getPrice() != null) {
            return product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }


}