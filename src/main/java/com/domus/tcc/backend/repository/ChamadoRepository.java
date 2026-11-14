package com.domus.tcc.backend.repository;

import com.domus.tcc.backend.domain.Chamado;
import com.domus.tcc.backend.domain.enums.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    List<Chamado> findAllByStatusChamado(StatusChamado statusChamado);

    List<Chamado> findAllByMorador_Id(Long idMorador);

    List<Chamado> findAllByStatusChamadoAndMorador_Id(StatusChamado statusChamado, Long idMorador);
}
