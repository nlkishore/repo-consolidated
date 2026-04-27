package com.oscar.pictures.entity;


import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
@Getter
@Setter
@Entity
@ToString
@Table(name="genres")
public class GenRes {

	@Id
	//@GeneratedValue(strategy  = GenerationType.IDENTITY )
	//@GeneratedValue(strategy  = GenerationType.UUID )
	@GeneratedValue(generator = "sequence_genre_id") 
	@GenericGenerator(name = "sequence_genre_id", strategy = "com.oscar.pictures.entity.GenResIDGenerator")
	@Column(nullable=false)
	private String gen_id;
	@Column(nullable=false)
	private String gen_title;
	@Column(nullable=false)
	private String gen_desc;
}
