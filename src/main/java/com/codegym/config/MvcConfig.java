package com.codegym.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MvcConfig.class);


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectPath = Paths.get("").toAbsolutePath().toString();
        LOGGER.info("Project absolute path: {}", projectPath);

        String uploadPath = projectPath + "/product-images/";
        LOGGER.info("Resource location for images: {}", uploadPath);
        registry.addResourceHandler("/product-images/**")
                .addResourceLocations("file:" + uploadPath);
    }



    private void exposeDirectory(String dirName, String userHome, ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(userHome, dirName);
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/" + dirName + "/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }
}