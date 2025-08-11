package com.vector.config;

import com.vector.utils.context.RequestThreadInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * <p>
 * 用于注册请求线程拦截器，确保所有请求都能正确标记和清除请求线程
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestThreadInterceptor requestThreadInterceptor;

    /**
     * 添加拦截器
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册请求线程拦截器，应用到所有URL
        registry.addInterceptor(requestThreadInterceptor)
                .addPathPatterns("/**");
    }
}
