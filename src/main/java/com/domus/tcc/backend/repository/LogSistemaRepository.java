package com.domus.tcc.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.domus.tcc.backend.infra.aop.LogSistema;


public interface LogSistemaRepository extends JpaRepository<LogSistema, Long> {
}