package com.domus.tcc.backend.repository;

import com.domus.tcc.backend.security.Papel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PapelRepository extends JpaRepository<Papel, Long> {
    Optional<Papel> findByNomePapel(String nomePapel);
}
