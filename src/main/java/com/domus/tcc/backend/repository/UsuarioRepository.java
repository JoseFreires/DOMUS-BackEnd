package com.domus.tcc.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;

import com.domus.tcc.backend.security.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.pessoa JOIN FETCH u.papel WHERE u.username = :username")
    UserDetails findByUsername(@Param("username") String username);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.pessoa JOIN FETCH u.papel WHERE LOWER(u.username) = LOWER(:username)")
    Optional<Usuario> findByUsernameIgnoreCase(@Param("username") String username);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.pessoa JOIN FETCH u.papel WHERE LOWER(u.pessoa.email) = LOWER(:email)")
    Optional<Usuario> findByPessoaEmailIgnoreCase(@Param("email") String email);

    @Query(""" 
            SELECT u FROM Usuario u
            JOIN FETCH u.pessoa p
            LEFT JOIN FETCH p.morador m
            LEFT JOIN FETCH m.moradia moradia
            LEFT JOIN FETCH moradia.bloco
            LEFT JOIN FETCH p.porteiro
            WHERE u.id = :id
            """)
    Optional<Usuario> findByIdComDadosPerfil(@Param("id") Long id);
}