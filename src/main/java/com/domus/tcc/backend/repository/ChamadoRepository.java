package com.domus.tcc.backend.repository;

import com.domus.tcc.backend.domain.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    List<Chamado> findByMorador_Id(Long idMorador);
}
