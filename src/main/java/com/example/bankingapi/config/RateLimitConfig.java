package com.example.bankingapi.config;

import com.example.bankingapi.RateLimiting.RateLimiter;
import com.example.bankingapi.RateLimiting.RateLimitInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {
    
    @Bean
    public RateLimiter rateLimiter() {
        return new RateLimiter(5, 60);
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor(rateLimiter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor())
                .addPathPatterns("/api/v1/**");
    }
}