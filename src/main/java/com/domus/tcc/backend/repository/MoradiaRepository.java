package com.domus.tcc.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.domus.tcc.backend.domain.Moradia;

@Repository
public interface MoradiaRepository extends JpaRepository<Moradia, Long>{

}