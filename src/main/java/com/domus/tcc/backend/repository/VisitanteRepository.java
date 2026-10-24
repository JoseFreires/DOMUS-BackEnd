package com.domus.tcc.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.domus.tcc.backend.domain.Visitante;

import java.util.Optional;

@Repository
public interface VisitanteRepository extends JpaRepository<Visitante, Long> {

    @Query("SELECT v FROM Visitante v WHERE v.id = :idVisitante")
    Optional<Visitante> findVisitanteById(@Param("idVisitante") Long idVisitante);
}