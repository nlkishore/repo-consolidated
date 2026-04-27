package com.oscar.pictures.config;

import java.time.Duration;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.VersionResourceResolver;

@Configuration
public class MovieGenResConfig {
	@Bean
	public ModelMapper getMapper () { 
		
		return new ModelMapper();
	}
	
	@Bean
	public RestTemplate getRestObject() {
		
		return new RestTemplate();
	}
	
	
}
