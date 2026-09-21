package com.domus.tcc.backend.repository;

import com.domus.tcc.backend.domain.AvisoCondominial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AvisosRepository extends JpaRepository<AvisoCondominial, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE AvisoCondominial a SET a.ativo = false WHERE a.dataValidadeAviso < :dataHoje AND a.ativo = true")
    int expirarAvisoVencidos(@Param("dataHoje") LocalDate dataHoje);

    @Query("SELECT a FROM AvisoCondominial a WHERE a.ativo = true AND (a.dataValidadeAviso IS NULL OR a.dataValidadeAviso >= :dataHoje)")
    List<AvisoCondominial> findAvisosAtivos(@Param("dataHoje") LocalDate dataHoje);

    @Query("SELECT a FROM AvisoCondominial a WHERE a.ativo = false")
    List<AvisoCondominial> findAvisosNaoAtivos();
}
