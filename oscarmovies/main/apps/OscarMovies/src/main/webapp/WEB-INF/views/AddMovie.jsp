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
       			<center>Add Movie</center>
       		</div>
       		<form method="post" action="commitaddmovie">
       				<!--<div class="row">
       					<input type="text" name="mov_id" class="form-control"
       					 placeholder="Enter ID"/>
       				</div>-->
       				<div class="row">
       					<input type="text" name="mov_title" class="form-control"
       					 placeholder="Enter Title"/>
       				</div>
       				<div class="row">
       					<input type="text" name="mov_year" class="form-control"
       					 placeholder="Enter Year"/>
       				</div>
       				<div class="row">
       					<input type="text" name="mov_time" class="form-control"
       					 placeholder="Enter Time"/>
       				</div>
       				<div class="row">
       					<input type="text" name="mov_lang" class="form-control"
       					 placeholder="Enter Language"/>
       				</div>
       					<div class="row">
       					<input type="text" name="mov_rel_country" class="form-control"
       					 placeholder="Enter Country"/>
       				</div>
     			    <div class="row">
       					<input type="submit" value="Add Movie" class="btn btn-success"/>
       				</div>

       		</form>
       </div>
    </body>
</html>