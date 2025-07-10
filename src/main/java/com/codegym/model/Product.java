package com.codegym.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Column(length = 1024) // Mô tả dài
    private String description;

    // ===== THÊM THUỘC TÍNH MỚI VÀO ĐÂY =====
    @Column(name = "short_description", length = 255)
    private String shortDescription;

    @Column(nullable = false)
    private BigDecimal price;


    @Column(nullable = true, length = 256)
    private String imageUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImage> images = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    public void addExtraImage(String imageName) {
        ProductImage image = new ProductImage();
        image.setName(imageName);
        image.setProduct(this);
        this.images.add(image);
    }

    @Transient
    public String getPhotosImagePath() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (id != null) {
                return "/product-images/" + this.id + "/" + this.imageUrl;
            }
        }

        // Nếu có ảnh, trả về đường dẫn đúng
        return "/images/default-product.png";
    }

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();
}
