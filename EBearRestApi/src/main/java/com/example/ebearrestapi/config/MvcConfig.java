package com.example.ebearrestapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectPath = System.getProperty("user.dir");
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:///" + projectPath + "/src/main/resources/static/files/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // “*“같은 와일드카드를 사용
                .allowedMethods("GET", "POST") // 허용할 HTTP method
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 쿠키 인증 요청 허용
    }
}
