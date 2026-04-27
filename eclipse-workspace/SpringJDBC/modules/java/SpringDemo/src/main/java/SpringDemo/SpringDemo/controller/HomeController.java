package SpringDemo.SpringDemo.controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import SpringDemo.SpringDemo.Models.Product;

@Controller
public class HomeController {
	
	public static ArrayList<Product> productList = new ArrayList<Product>();

	static {
		productList.add(new Product(101, "Test Product1", 25000));
		productList.add(new Product(102, "Test Product2", 36000));
		productList.add(new Product(103, "Test Product3", 41500));
		productList.add(new Product(104, "Test Product4", 59800));
	}
	
	
	@RequestMapping(value="/",method = RequestMethod.GET)
	public ModelAndView Dashboard()
	{
		ModelAndView modelandview=new ModelAndView("home");
		modelandview.addObject("products",productList);
		return modelandview;
	}
	
	@RequestMapping(value="/add")
	public String addProduct() {
		return "Add";
	}
	
	@RequestMapping(value="/add", method=RequestMethod.POST)
	public String addProduct(@ModelAttribute("product") Product prod) {
		System.out.println(prod);
		productList.add(prod);
		HomeController.getData();
		//return "Add";
		return "redirect:/";
	}
	
	private static void getData() {
		for (int i=0; i < productList.size();i++) {
			Product prod = productList.get(i);
			System.out.println(prod);
		}
		
	}
	 
	@RequestMapping(value="/delete/{id}" ,method=RequestMethod.GET)
	public String deleteProduct(@PathVariable int id) {
		System.out.println( " id  "+id);
		Product prod = this.getProductById(id);
		productList.remove(prod);
		return "redirect:/";
	}
	
	Product getProductById(int id) {
		//productList.forEach(prod->prod.getId()==id);
		for (Product product: productList) {
			if (product.getId() == id) {
				return product;
			}
		}
		
		return null;
	}
	
	@RequestMapping(value="edit/{id}", method=RequestMethod.GET)
	public ModelAndView Edit(@PathVariable int id) {
		ModelAndView mav = new ModelAndView("Edit");
		Product prod = this.getProductById(id);
		mav.addObject("product",prod);
		return mav;
	}
	
	
	@RequestMapping(value="/edit/{id}",method=RequestMethod.POST)
	public String Edit (@PathVariable int id,@ModelAttribute Product prod) {
		
		for(Product product:productList)
		{
			if(product.getId()==id)
			{
				product.setName(prod.getName());
				product.setPrice(prod.getPrice());
				break;
			}
		}
		return "redirect:/";
	}
}
