package com.domus.tcc.backend.repository;

import com.domus.tcc.backend.domain.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
}
