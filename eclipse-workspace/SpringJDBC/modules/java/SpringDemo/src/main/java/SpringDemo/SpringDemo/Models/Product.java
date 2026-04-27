package SpringDemo.SpringDemo.Models;

public class Product {
private int id;
private String name;
private double Price;
public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public double getPrice() {
	return Price;
}
public void setPrice(double price) {
	Price = price;
}
@Override
public String toString() {
	return "Product [id=" + id + ", name=" + name + ", Price=" + Price + "]";
}
public Product(int id, String name, double price) {
	super();
	this.id = id;
	this.name = name;
	Price = price;
}
public Product() {
	super();
	// TODO Auto-generated constructor stub
}

}
