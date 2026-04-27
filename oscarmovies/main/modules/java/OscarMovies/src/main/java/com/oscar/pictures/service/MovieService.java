package com.oscar.pictures.service;

import java.util.List;

import com.oscar.pictures.entity.Movie;

public interface MovieService {
	
	public Movie addMovie(Movie movie);
	public Movie updateMovie(Movie movie,String Id);
	public void deleteMovie(Movie movie);
	public Movie getMovieById(String id);
	public List<Movie> getAllMovies();

}
