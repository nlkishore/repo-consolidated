       <%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Home</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
    </head>
    <body>
       <div class="container mt-3">
       		<div class="alert alert-success">
       			<center>Edit Product</center>
       		</div>
       		<form method="post" >
       			<div class="row">
       				<div class="col">
       					<input type="text" name="id" class="form-control"
       					 placeholder="Enter Id" value="${product.id}"/>
       				</div>
       				<div class="col">
       					<input type="text" name="name" class="form-control"
       					 placeholder="Enter Name" value="${product.name}"/>
       				</div>
       			</div>
       			<div class="row mt-3 ">
       				<div class="col">
       					<input type="number" name="price" class="form-control"
       					 placeholder="Enter Price" value="${product.price}"/>
       				</div>
       			</div>
       			<div class="row mt-3 ">
       				<div class="col">
       					<input type="submit" value="Edit Product" class="btn btn-success"/>
       				</div>
       			</div>
       		</form>
       </div>
    </body>
</html>