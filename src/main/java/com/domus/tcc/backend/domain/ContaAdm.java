package com.domus.tcc.backend.domain;


import java.util.Collection;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.domus.tcc.backend.dto.DadosLoginDTO;
import com.domus.tcc.backend.security.Papel;

@Getter
@Entity(name = "ContaAdm")
@Table(name = "conta_adm")
public class ContaAdm implements UserDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idConta")
    private Long idContaAdm;

    @Column(name = "nome_conta", nullable = false)
    private String nomeConta;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String senha;

    private Boolean ativo;

    @OneToOne
    @JoinColumn(name = "Papel_idPapel")
    private Papel papel;

   
    public ContaAdm(DadosLoginDTO dados, String senhaCriptografada) {
        this.username = dados.username();
        this.senha = senhaCriptografada;
    }

    public ContaAdm() {

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (papel != null) {
            return List.of(new SimpleGrantedAuthority(papel.getNomePapel()));
        }
        return List.of();
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    


}
