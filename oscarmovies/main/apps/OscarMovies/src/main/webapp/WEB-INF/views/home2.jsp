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
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.6/dist/umd/popper.min.js" integrity="sha384-oBqDVmMz9ATKxIep9tiCxS/Z9fNfEXiDAYTujMAeBAsjFuCZSmKbSSUnQlmh/jp3" crossorigin="anonymous"></script>
		<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.min.js" integrity="sha384-cuYeSxntonz0PPNlHhBs68uyIAVpIIOZZ5JqeqvYYIcEL727kskC66kF92t6Xl2V" crossorigin="anonymous"></script>
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    	<script type="text/javascript" th:src="@{/js/functions.js}"></script>
    	<script src="js/custom.js"></script>
    </head>
    <body>
    <div class="container">
    	<h1>Genres List</h1>
    	 <div align="right"><input type="submit" value="Add Genre" class="btn btn-success" onclick="addGenres()"/></div>
		 <table id="details" class="table table-responsive table-bordered table-hover mt-2">
        		<thead>
        			<tr>
        				<th>Genre ID</th>
        				<th>Genre Title</th>
        				<th>Genre Desc</th>

        			</tr>
        		</thead>
        		<tbody>
        			<c:forEach var="genre" items="${genres}">
        				<tr>
        					<td>${genre.gen_id} </td>
        					<td>${genre.gen_title} </td>
        					<td>${genre.gen_desc} </td>
        				</tr>
        			</c:forEach>
        		</tbody>
        	</table>
	</div>
    </body>
</html>