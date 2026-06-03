package com.atamanahmet.vinylexchange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class PageableConfig {
    @Bean
    PageableHandlerMethodArgumentResolverCustomizer pageableCustumizer() {
        return resolver -> resolver.setMaxPageSize(75);
    }
}
