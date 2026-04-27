package com.oscar.pictures.service;

import java.util.List;

import com.oscar.pictures.entity.GenRes;
import com.oscar.pictures.entity.Movie;
import com.oscar.pictures.repository.GenresRepository;
import com.oscar.pictures.repository.MovieRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenResServiceImpl implements GenresService{
	@Autowired
    private GenresRepository genresRepository;

	@Override
	public GenRes addGenres(GenRes prod) {
		// TODO Auto-generated method stub
		return genresRepository.save(prod);
	}

	@Override
	public GenRes updateGenres(GenRes prod, String Id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteGenres(GenRes prod) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public GenRes getGenresById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GenRes> getAllGenres() {
		return (List<GenRes>)
	            genresRepository.findAll();
	}

}
