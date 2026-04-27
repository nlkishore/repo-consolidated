package com.uob.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Framework 6.x Configuration
 * Integrates Spring with Turbine 7 application
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.uob")
public class SpringConfig implements WebMvcConfigurer {

    /**
     * Configure JSON message converter for REST APIs
     */
    @Bean
    public MappingJackson2HttpMessageConverter jsonMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }
}
