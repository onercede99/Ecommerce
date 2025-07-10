package com.codegym.config; // Hoặc package của bạn

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // Thêm một logger để in ra console
    private static final Logger LOGGER = LoggerFactory.getLogger(MvcConfig.class);

    public MvcConfig() {
        // Dòng log này sẽ xuất hiện khi Spring tạo bean MvcConfig
        LOGGER.info("================= MvcConfig IS BEING LOADED! =================");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path projectDir = Paths.get("").toAbsolutePath();
        Path uploadDir = projectDir.resolve("product-images");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // ... các dòng log giữ nguyên ...

        // THAY ĐỔI DUY NHẤT Ở ĐÂY: "file:/" -> "file:///"
        registry.addResourceHandler("/product-images/**")
                .addResourceLocations("file:///" + uploadPath + "/");
    }
}