package com.domus.tcc.backend.services;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.domus.tcc.backend.domain.ContaAdm;
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
                .withIssuer("Domus")
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
                    .withIssuer("Domus")
                    .withSubject(contaAdm.getUsername())
                    .withClaim("roles", roles)
                    .withClaim("nome", nome)
                    .withExpiresAt(Expiracao())
                    .sign(algorithm);


        }catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token", exception);
        }



    }

    public String gerarTokenRedefinicaoSenha(String username) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(segredo.trim());

            return JWT.create()
                    .withIssuer("Domus")
                    .withSubject(username)
                    .withClaim("tipo", "redefinicao_senha")
                    .withExpiresAt(ExpiracaoRedefinicaoSenha())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token de redefinição", exception);
        }
    }

    public String validarTokenRedefinicaoSenha(String tokenJwt) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(segredo.trim());
            var decoded = JWT.require(algorithm)
                    .withIssuer("Domus")
                    .build()
                    .verify(tokenJwt);

            String tipo = decoded.getClaim("tipo").asString();
            if (!"redefinicao_senha".equals(tipo)) {
                throw new JWTVerificationException("Token inválido para redefinição de senha");
            }

            return decoded.getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token de redefinição inválido ou expirado", exception);
        }
    }

    private Instant Expiracao(){

    return OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"))
                     .plusHours(2)
                     .toInstant();
    }

    private Instant ExpiracaoRedefinicaoSenha(){
        return OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"))
                .plusMinutes(15)
                .toInstant();
    }

    public String getSubject(String tokenJwt){

        try {
            Algorithm  algorithm = Algorithm.HMAC256(segredo);
            return   JWT.require(algorithm)
                .withIssuer("Domus")
                .build()
                .verify(tokenJwt)
                .getSubject();
                
        } catch (JWTVerificationException exception){
            throw new RuntimeException("Token JWT inválido ou expirado", exception);
        }
    }
    
}