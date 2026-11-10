package com.domus.tcc.backend.repository;

import com.domus.tcc.backend.domain.TipoVisita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoVisitaRepository extends JpaRepository<TipoVisita, Long> {
}
