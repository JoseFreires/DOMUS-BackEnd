package com.domus.tcc.backend.repository;

import com.domus.tcc.backend.domain.Encomenda;
import com.domus.tcc.backend.domain.PessoaAutorizada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PessoaAutorizadaRepository extends JpaRepository<PessoaAutorizada, Long> {

    @Query("SELECT p FROM PessoaAutorizada p WHERE p.morador.id = :idMorador")
    List<PessoaAutorizada> findByMoradorId(@Param("idMorador") Long idMorador);
}
