package com.appcenter.BJJ.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${storage.images.review}")
    private String REVIEW_IMG_DIR;

    @Value("${storage.images.item}")
    private String ITEM_IMG_DIR;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/review/**")
                .addResourceLocations("file:///" + REVIEW_IMG_DIR);
        registry.addResourceHandler("/images/item/**")
                .addResourceLocations("file:///" + ITEM_IMG_DIR);
    }
}
