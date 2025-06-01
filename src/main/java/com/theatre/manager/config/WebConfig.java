package com.theatre.manager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // для встроенных картинок
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        // для фоток из папки uploads
        registry.addResourceHandler("/uploads/photos/**")
                .addResourceLocations("file:///C:/Users/Алексей/Desktop/theatre-manager/uploads/photos/");
    }
}
