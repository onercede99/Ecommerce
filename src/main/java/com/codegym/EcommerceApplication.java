package com.codegym;


import com.codegym.model.Product;
import com.codegym.model.Role;
import com.codegym.repository.ProductRepository;
import com.codegym.repository.RoleRepository;
import com.codegym.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;


import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

@SpringBootApplication
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository, UserRepository userRepository,
                                   PasswordEncoder passwordEncoder, ProductRepository productRepository) {
        return args -> {
            if (roleRepository.findByName("ROLE_STAFF").isEmpty()) {
                Role staffRole = new Role();
                staffRole.setName("ROLE_STAFF");
                roleRepository.save(staffRole);
                LOGGER.info("Created ROLE_STAFF");
            }
            if (productRepository.count() == 0) {
                Product p1 = new Product();
                p1.setName("Clean Code");
                p1.setDescription("A Handbook of Agile Software Craftsmanship by Robert C. Martin");
                p1.setPrice(new BigDecimal("45.50"));
                p1.setImageUrl("https://m.media-amazon.com/images/I/41xShlnl+IL._SY445_SX342_.jpg");
                productRepository.save(p1);

                Product p2 = new Product();
                p2.setName("Designing Data-Intensive Applications");
                p2.setDescription("The Big Ideas Behind Reliable, Scalable, and Maintainable Systems");
                p2.setPrice(new BigDecimal("55.00"));
                // URL mới
                p2.setImageUrl("https://m.media-amazon.com/images/I/51pX9a2R3PL._SY445_SX342_.jpg");
                productRepository.save(p2);

                Product p3 = new Product();
                p3.setName("The Pragmatic Programmer");
                p3.setDescription("Your Journey to Mastery, 20th Anniversary Edition");
                p3.setPrice(new BigDecimal("49.99"));
                p3.setImageUrl("https://m.media-amazon.com/images/I/51yax2KiLpL._SY445_SX342_.jpg");
                productRepository.save(p3);
            }
        };
    }
}
