package com.domus.tcc.backend.repository;

import com.domus.tcc.backend.domain.Encomenda;
import com.domus.tcc.backend.domain.enums.StatusEncomenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EncomendaRepository extends JpaRepository<Encomenda, Long> {

    @Query("SELECT e FROM Encomenda e JOIN FETCH e.moradorDestinatario JOIN FETCH e.porteiro")
    List<Encomenda> findAll();

    @Query("SELECT e FROM Encomenda e JOIN FETCH e.moradorDestinatario JOIN FETCH e.porteiro WHERE e.statusEncomenda = :status")
    List<Encomenda> findByStatus(@Param("status") StatusEncomenda status);

    @Query("SELECT e FROM Encomenda e JOIN FETCH e.moradorDestinatario m JOIN FETCH e.porteiro WHERE m.id = :idMorador")
    List<Encomenda> findByMoradorId(@Param("idMorador") Long idMorador);
}