package com.example.SpringMVC.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.example.SpringMVC.Models.ProductDAO;
import com.example.SpringMVC.config.AppConfig;



import com.example.SpringMVC.Models.Product;

@Controller
public class HomeController {

	/*
	 * @RequestMapping(value="/") public ModelAndView test(HttpServletResponse
	 * response) throws IOException{ return new ModelAndView("home"); }
	 */
	
	@RequestMapping(value="/",method = RequestMethod.GET)
	public ModelAndView Dashboard()
	{
		ApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class);
		ProductDAO dao=(ProductDAO)context.getBean(ProductDAO.class);
		//List<Product>products= dao.GetProducts();// Method to fecth record using Simple SQL
		
		List<Product>products= dao.GetAllProducts(); // Method to fetch the record using Prepared statement
		for(Product prod:products)
		{
			System.out.println(prod);
		}
		
		ModelAndView mav=new ModelAndView("home");	
		mav.addObject("products",products);
		return mav;
	}
	
	@RequestMapping(value="/delete/{id}",method=RequestMethod.GET)
	public String deleteProduct(@PathVariable int id)
	{
		System.out.println("Id received is:"+id);
		ApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class);
		ProductDAO dao=(ProductDAO)context.getBean(ProductDAO.class);
		dao.delete(id);
		
		return "redirect:/";
	}
	

	@RequestMapping(value="/add")
	public String addProduct() {
		return "Add";
	}
	
	@RequestMapping(value="/add", method=RequestMethod.POST)
	public String addProduct(@ModelAttribute("product") Product prod) {
		System.out.println(prod);
		ApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class);
		ProductDAO dao=(ProductDAO)context.getBean(ProductDAO.class);
		//dao.Add(prod); // Method to ad record using simple Sql statement
		//dao.insert(prod);//Method to add record using prepared Statement
		
		int rowcount=dao.insert(prod);//Method to add record using prepared Statement
		if(rowcount>0)
		{
			System.out.println("Inserted Successfully!");
		}
		else
		{
			System.out.println("Failed to Insert!");
		}		
		return "redirect:/";
	}
	
	
	@RequestMapping(value="edit/{id}", method=RequestMethod.GET)
	public ModelAndView Edit(@PathVariable int id) {
		ModelAndView mav = new ModelAndView("Edit");
		//Product prod = this.getProductById(id);
		ApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class);
		ProductDAO dao=(ProductDAO)context.getBean(ProductDAO.class);
		Product prod = dao.getProduct(id);
		mav.addObject("product",prod);

		return mav;
	}

	

	@RequestMapping(value="/edit1/{id}",method = RequestMethod.GET)
	public ModelAndView Edit1(@PathVariable int id)
	{
		
		ApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class);
		ProductDAO dao=context.getBean(ProductDAO.class);
		Product prod=dao.GetProductById(id);
		System.out.println(prod);
		
		ModelAndView mav=new ModelAndView("Edit");
		mav.addObject("product",prod);
		return mav;
	}
	
	@RequestMapping(value="/edit1/{id}",method=RequestMethod.POST)
	public String Edit(@PathVariable int id,@ModelAttribute Product prod)
	{
		ApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class);
		ProductDAO dao=context.getBean(ProductDAO.class);
		int rowcount=dao.update(prod);
		if(rowcount>0)
		{
			System.out.println("Updated Successfully!");
		}
		else
		{
			System.out.println("Failed to Update!");
		}
		return "redirect:/";
	}
	
	@RequestMapping(value="/delete1/{id}",method=RequestMethod.GET)
	public String Delete(@PathVariable int id)
	{
		ApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class);
		ProductDAO dao=context.getBean(ProductDAO.class);
		int rowcount=dao.delete(id);
		if(rowcount>0)
		{
			System.out.println("Deleted Successfully!");
		}
		else
		{
			System.out.println("Failed to Delete!");
		}
		return "redirect:/";
	}
	
}
