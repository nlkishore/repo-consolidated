package com.oscar.pictures.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.oscar.pictures.entity.Movie;
import com.oscar.pictures.repository.MovieRepository;

@Service
public class MovieServiceImpl implements MovieService {

	@Autowired
    private MovieRepository movieRepository;
	@Override
	public Movie addMovie(Movie movie) {
		// TODO Auto-generated method stub
		return movieRepository.save(movie);
	}

	@Override
	public Movie updateMovie(Movie prod, String Id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteMovie(Movie prod) {
		// TODO Auto-generated method stub

	}

	@Override
	public Movie getMovieById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Movie> getAllMovies() {
		return (List<Movie>)movieRepository.findAll();
		
	}

}
