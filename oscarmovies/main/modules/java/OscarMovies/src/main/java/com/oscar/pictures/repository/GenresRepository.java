package com.oscar.pictures.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oscar.pictures.entity.GenRes;

public interface GenresRepository extends JpaRepository<GenRes, String> {

}
