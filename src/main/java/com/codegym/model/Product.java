package com.codegym.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
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

    private String description;

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
        if (imageUrl == null || id == null) return null;
        return "/product-images/" + this.id + "/" + this.imageUrl;
    }
}
