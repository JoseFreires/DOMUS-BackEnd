package com.domus.tcc.backend.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.domus.tcc.backend.dto.DadosLoginDTO;
import com.domus.tcc.backend.domain.Pessoa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "Usuario")
@Table(name = "usuario")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String senha;

    // Relacionamento 1:1 com Pessoa (Um usuário é uma pessoa específica)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Pessoa_idPessoa", nullable = false)
    private Pessoa pessoa;

    // Relacionamento Many-to-One com Papel
    // Muitos usuários podem ter o mesmo papel
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Papel_idPapel")
    private Papel papel;

    public Usuario(DadosLoginDTO dados, String senhaCriptografada) {
        this.username = dados.username();
        this.senha = senhaCriptografada;
    }

    public Usuario() {

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
