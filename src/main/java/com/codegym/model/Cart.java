package com.codegym.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "carts")
@Getter
@Setter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Quan hệ 1-N với các món hàng trong giỏ
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<CartItemDb> items = new HashSet<>();

    @Transient
    public int getTotalItems() {
        if (items == null) {
            return 0;
        }
        return items.stream()
                .mapToInt(CartItemDb::getQuantity)
                .sum();
    }

    @Transient
    public BigDecimal getTotalPrice() {
        if (items == null) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(CartItemDb::getSubtotal) // Lấy subtotal (đã là BigDecimal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}