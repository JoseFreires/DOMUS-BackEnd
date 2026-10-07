package com.domus.tcc.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.domus.tcc.backend.domain.Visitante;


@Repository
public interface VisitanteRepository extends JpaRepository <Visitante, Long>{
    
    List<Visitante> findByMoradiasId(Long idMoradia);


}