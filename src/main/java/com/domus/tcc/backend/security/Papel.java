package com.domus.tcc.backend.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Getter
@Setter
@Table(name = "papel")
public class Papel implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPapel")
    private Long idPapel;

    @Column(name = "nome_papel", nullable = false, unique = true, length = 45)
    private String nomePapel;

    @Override
    public String getAuthority() {
        return this.nomePapel;
    }

}