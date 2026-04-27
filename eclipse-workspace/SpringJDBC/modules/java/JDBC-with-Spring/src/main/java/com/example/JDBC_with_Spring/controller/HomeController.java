package com.example.JDBC_with_Spring.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.JDBC_with_Spring.Models.Product;
import com.example.JDBC_with_Spring.Models.ProductDAO;
import com.example.JDBC_with_Spring.config.AppConfig;

//import com.example.SpringMVC.Models.Product;
//import com.example.SpringMVC.Models.ProductDAO;
//import com.example.SpringMVC.config.AppConfig;

@Controller
public class HomeController {

	@RequestMapping(value="/")
	public ModelAndView test(HttpServletResponse response) throws IOException{
		
		ApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class);
		ProductDAO dao=(ProductDAO)context.getBean(ProductDAO.class);
		List<Product>products= dao.GetAllProducts();
		
		for(Product prod:products)
		{
			System.out.println(prod);
		}
		
		ModelAndView mav=new ModelAndView("home");	
	//	mav.addObject("products",products);
		return mav;
	
		//return new ModelAndView("home");
	}
}
