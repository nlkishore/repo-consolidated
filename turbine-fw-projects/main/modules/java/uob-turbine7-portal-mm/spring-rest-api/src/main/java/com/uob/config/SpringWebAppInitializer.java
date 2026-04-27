package com.uob.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Spring Web Application Initializer
 * 
 * Note: DispatcherServlet is already configured in web.xml,
 * so this initializer only sets up the root application context.
 * The DispatcherServlet in web.xml will use SpringConfig for its context.
 */
public class SpringWebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // Create root application context for shared beans
        // The DispatcherServlet in web.xml will create its own child context
        // using SpringConfig via contextConfigLocation parameter
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(SpringConfig.class);
        
        // Register Spring context loader listener for root context
        servletContext.addListener(new ContextLoaderListener(rootContext));
        
        // Note: DispatcherServlet is already registered in web.xml with:
        // - servlet-name: springDispatcher
        // - url-pattern: /api/*
        // - contextConfigLocation: com.uob.config.SpringConfig
        // So we don't need to register it here programmatically
    }
}
