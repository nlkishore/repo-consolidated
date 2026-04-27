<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
   <html xmlns:th="http://www.thymeleaf.org">
   <%--%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %--%>
   <%@ taglib prefix="c" uri="jakarta.tags.core" %>
   <html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Home</title>
        <link href="css/custom.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.6/dist/umd/popper.min.js" integrity="sha384-oBqDVmMz9ATKxIep9tiCxS/Z9fNfEXiDAYTujMAeBAsjFuCZSmKbSSUnQlmh/jp3" crossorigin="anonymous"></script>
		<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.min.js" integrity="sha384-cuYeSxntonz0PPNlHhBs68uyIAVpIIOZZ5JqeqvYYIcEL727kskC66kF92t6Xl2V" crossorigin="anonymous"></script>
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    	<script type="text/javascript" th:src="@{/js/custom.js}"></script>
    	

    </head>
    <body>
      
        <div class="mr-2 mt-2">
        	<label ><small><strong>Movie List:</strong></small></label>
		</div>
		<div align="right"><input type="submit" value="Add Movie" class="btn btn-success" onclick="addMovie()"/></div>
        	<table class="table table-responsive table-bordered table-hover mt-2">
        		<thead>
        			<tr>
        				<th>Movie ID</th>
        				<th>Movie Title</th>
        				<th>Movie Year</th>
        				<th>Language</th>
        				<th>Country Released</th>
        				<!--<th>Actions</th>-->
        			</tr>
        		</thead>
        		<tbody>
        			<c:forEach var="movie" items="${movies}">
        				<tr>
        					<td>${movie.mov_id} </td>
        					<td>${movie.mov_title} </td>
        					<td>${movie.mov_year} </td>
        					<td>${movie.mov_lang} </td>
        					<td>${movie.mov_rel_country} </td>
        					<!--<td>
        						<a href="<c:url value='/edit/${movie.mov_id}'/>" class="btn btn-warning">Edit</a>
        						&nbsp;
        						<a href="<c:url value='/delete/${movie.mov_id}'/>" class="btn btn-danger">Delete</a>
        					</td>-->
        				</tr>
        			</c:forEach>
        		</tbody>
        	</table>
        </div>
    </body>
</html>