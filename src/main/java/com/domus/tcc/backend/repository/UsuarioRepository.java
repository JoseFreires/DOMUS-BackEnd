package com.domus.tcc.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;

import com.domus.tcc.backend.security.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{

    @Query("SELECT u FROM Usuario u JOIN FETCH u.pessoa JOIN FETCH u.papel WHERE u.username = :username")
    UserDetails findByUsername(@Param("username") String username);


}