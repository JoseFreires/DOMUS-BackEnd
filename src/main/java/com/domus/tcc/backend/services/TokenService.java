package com.domus.tcc.backend.services;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import com.domus.tcc.backend.domain.ContaAdm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.domus.tcc.backend.security.Usuario;


@Service
public class TokenService{
    
    @Value("${api.security.tokenJWT.segredo}")
    private  String segredo;

    public String gerarTokenUsuario(Usuario usuario){
        try {
            
            Algorithm  algorithm = Algorithm.HMAC256(segredo.trim());
          
            List<String> roles = usuario.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

            String nome = usuario.getPessoa().getNomeCompleto();

            return JWT.create()
                .withIssuer("Hermes")
                .withSubject(usuario.getUsername()) 
                .withClaim("roles", roles)
                    .withClaim("nome", nome)
                .withExpiresAt(Expiracao())
                .sign(algorithm);

                
        }catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token", exception);
        }

       

    }

    public String gerarTokenContaAdm(ContaAdm contaAdm){
        try {

            Algorithm  algorithm = Algorithm.HMAC256(segredo.trim());

            List<String> roles = contaAdm.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String nome = contaAdm.getNomeConta();

            return JWT.create()
                    .withIssuer("Hermes")
                    .withSubject(contaAdm.getUsername())
                    .withClaim("roles", roles)
                    .withClaim("nome", nome)
                    .withExpiresAt(Expiracao())
                    .sign(algorithm);


        }catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token", exception);
        }



    }

    private Instant Expiracao(){

    return OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"))
                     .plusHours(2)
                     .toInstant();
    }


    public String getSubject(String tokenJwt){

        try {
            Algorithm  algorithm = Algorithm.HMAC256(segredo);
            return   JWT.require(algorithm)
                .withIssuer("Hermes")
                .build()
                .verify(tokenJwt)
                .getSubject();
                
        } catch (JWTVerificationException exception){
            throw new RuntimeException("Token JWT inválido ou expirado", exception);
        }
    }
    
}