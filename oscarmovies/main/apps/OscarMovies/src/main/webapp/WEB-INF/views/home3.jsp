<!DOCTYPE html>
<html lang="en">
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title> Oscar Movies List bz Genres</title>
        <link rel="stylesheet" type="text/css" href="css/custom.css" />
        <link rel="stylesheet" href="css/custom.css">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.6/dist/umd/popper.min.js" integrity="sha384-oBqDVmMz9ATKxIep9tiCxS/Z9fNfEXiDAYTujMAeBAsjFuCZSmKbSSUnQlmh/jp3" crossorigin="anonymous"></script>
		<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.min.js" integrity="sha384-cuYeSxntonz0PPNlHhBs68uyIAVpIIOZZ5JqeqvYYIcEL727kskC66kF92t6Xl2V" crossorigin="anonymous"></script>
    	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    	<script src="js/custom.js"></script>
</head>
<body class="bg-info text-white">
<h3 align="center">
    <i>OSCAR MOVIES LIST NOMINATED BY GENRE
    
</h3>
<div class="d-inline-flex mt-2 gap-2 mr-2">

      <div class=" mt-2 ml-2">
        <label class="mr-2 px-2" for="genres_id"><small><strong>Movie Genres:</strong></small></label>
      </div>
      <div class="ml-2">
         <select class="form-control form-control-sm mt-2 px-4" id="genres_id" name="genres_id" onchange="genres_id_change()">
         		<option value="0">Select GenRes Value</option>
                <c:forEach var="genre" items="${genres}">
                    <option id='gen_id' value="${genre.gen_id}" ${genre.gen_id == param.genres_id ? 'selected' : ''}>${genre.gen_title}</option>
                </c:forEach>
            </select>
      </div>
      <div class=" mt-2 ml-2">
      <!--<input type="submit" value="All Genres" class="form-control form-control-sm btn btn-success" click="genres_id_change1()"></input>-->
      <button type="button" class="btn btn-primary" onclick="getAllGenres()">All Genres</button>
      </div>
      <div class=" mt-2 ml-2">
      <button type="button" class="btn btn-primary" onclick="getAllMovies()">All Movies</button>
      </div>
  </div>
    <table id="details" class="table table-responsive table-bordered table-hover mt-2">
        		<thead>
        			<tr>
        				<th>Movie ID</th>
        				<th>Movie Title</th>
        				<th>Movie Year</th>
        				<th>Language</th>
        				<th>Country Released</th>
        				<th>Actions</th>
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
        					<td>
        						<a href="<c:url value='/edit/${movie.mov_id}'/>" class="btn btn-warning">Edit</a>
        						&nbsp;
        						<a href="<c:url value='/delete/${movie.mov_id}'/>" class="btn btn-danger">Delete</a>
        					</td>
        				</tr>
        			</c:forEach>
        		</tbody>
        	</table>
        </div>
</body>
</html>