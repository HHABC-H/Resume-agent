package com.h.resumeagent.config;

import com.h.resumeagent.auth.AuthTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthTokenInterceptor authTokenInterceptor;

    public WebMvcConfig(AuthTokenInterceptor authTokenInterceptor) {
        this.authTokenInterceptor = authTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authTokenInterceptor)
                .addPathPatterns("/api/resume/**", "/api/interview/**")
                .excludePathPatterns("/api/auth/**");
    }
}
