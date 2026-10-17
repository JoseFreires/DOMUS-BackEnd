package com.domus.tcc.backend.repository;

import com.domus.tcc.backend.domain.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
}
