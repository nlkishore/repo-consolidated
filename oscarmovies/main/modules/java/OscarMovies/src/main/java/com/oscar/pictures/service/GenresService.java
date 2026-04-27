package com.oscar.pictures.service;

import java.util.List;

import com.oscar.pictures.entity.GenRes;


public interface GenresService {
	public GenRes addGenres(GenRes genre);
	public GenRes updateGenres(GenRes genre,String Id);
	public void deleteGenres(GenRes genre);
	public GenRes getGenresById(String id);
	public List<GenRes> getAllGenres();

}
