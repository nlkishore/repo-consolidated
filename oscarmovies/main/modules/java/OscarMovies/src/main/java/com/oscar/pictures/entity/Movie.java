package com.oscar.pictures.entity;

import lombok.*;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
@Getter
@Setter
@Entity
@ToString
@Table(name="movie")
public class Movie {
	@Id
	@GeneratedValue(generator = "sequence_mov_id") 
	@GenericGenerator(name = "sequence_mov_id", strategy = "com.oscar.pictures.entity.MovieIDGenerator")
	@Column(nullable=false)
	private String mov_id;
	@Column(nullable=false)
	private String mov_title;
	@Column(nullable=false)
	private int mov_year;
	private int mov_time;
	private String mov_lang ;
	private String mov_rel_country ;
	
}

