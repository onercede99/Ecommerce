package com.codegym.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "product_images")
@Getter
@Setter
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name; // Tên file ảnh (ví dụ: "image-2.jpg")

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Phương thức trợ giúp để lấy đường dẫn ảnh hoàn chỉnh
    @Transient
    public String getImagePath() {
        if (name == null || product.getId() == null) return null;
        return "/product-images/" + product.getId() + "/" + this.name;
    }
}