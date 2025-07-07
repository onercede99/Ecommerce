package com.codegym.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = true, length = 256)
    private String imageUrl;

    @Transient
    public String getPhotosImagePath() {
        if (imageUrl == null || id == null) return null;
        return "/product-images/" + this.id + "/" + this.imageUrl;
    }
}
