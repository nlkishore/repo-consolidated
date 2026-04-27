package com.oscar.pictures.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oscar.pictures.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, String> {

}
