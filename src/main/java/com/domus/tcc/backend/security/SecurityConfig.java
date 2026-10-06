package com.domus.tcc.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> {

                // Autenticação
                request.requestMatchers(HttpMethod.POST, "/auth/entrar").permitAll();
                request.requestMatchers(HttpMethod.POST, "/auth/sair").permitAll();
                request.requestMatchers(HttpMethod.GET, "/auth/eu").permitAll();

                // ENCOMENDAS
                request.requestMatchers(HttpMethod.POST, "/encomendas").hasAnyRole("PORTEIRO", "ADMIN");
                request.requestMatchers(HttpMethod.GET, "/encomendas").hasAnyRole("PORTEIRO", "MORADOR", "SINDICO", "ADMIN");
                request.requestMatchers(HttpMethod.PUT, "/encomendas/**").hasAnyRole("PORTEIRO", "ADMIN");
                request.requestMatchers(HttpMethod.PATCH, "/encomendas/**").hasAnyRole("PORTEIRO", "ADMIN");
                request.requestMatchers(HttpMethod.DELETE, "/encomendas/**").hasAnyRole("PORTEIRO", "ADMIN");

                // MORADORES
                 request.requestMatchers(HttpMethod.POST, "/moradores").hasAnyRole("SINDICO", "ADMIN");
                request.requestMatchers(HttpMethod.GET, "/moradores").hasAnyRole("SINDICO", "ADMIN", "PORTEIRO");
                request.requestMatchers(HttpMethod.GET, "/moradores/**").hasAnyRole("MORADOR", "SINDICO", "ADMIN","PORTEIRO");
                request.requestMatchers(HttpMethod.PUT, "/moradores/**").hasAnyRole("MORADOR","SINDICO", "ADMIN");
                request.requestMatchers(HttpMethod.DELETE, "/moradores/**").hasAnyRole("ADMIN", "SINDICO");

                // PESSOAS AUTORIZADAS
                request.requestMatchers(HttpMethod.POST, "/pessoas-autorizadas").hasAnyRole("MORADOR");
                request.requestMatchers(HttpMethod.GET, "/pessoas-autorizadas").hasAnyRole("MORADOR", "PORTEIRO", "ADMIN");
                request.requestMatchers(HttpMethod.PUT, "/pessoas-autorizadas/**").hasAnyRole("MORADOR");
                request.requestMatchers(HttpMethod.DELETE, "/pessoas-autorizadas/**").hasAnyRole("MORADOR");

                // CONDOMINIOS
                request.requestMatchers(HttpMethod.GET, "/condominios").hasAnyRole("ADMIN", "SINDICO");
                request.requestMatchers(HttpMethod.POST, "/condominios").hasAnyRole("ADMIN");
                request.requestMatchers(HttpMethod.PUT, "/condominios/**").hasAnyRole("ADMIN");
                request.requestMatchers(HttpMethod.DELETE, "/condominios/**").hasAnyRole("ADMIN");

                // BLOCOS
                request.requestMatchers(HttpMethod.GET, "/blocos").hasAnyRole("ADMIN", "SINDICO");
                request.requestMatchers(HttpMethod.POST, "/blocos").hasAnyRole("ADMIN");
                request.requestMatchers(HttpMethod.PUT, "/blocos/**").hasAnyRole("ADMIN");
                request.requestMatchers(HttpMethod.DELETE, "/blocos/**").hasAnyRole("ADMIN");

                // MORADIAS
                request.requestMatchers(HttpMethod.GET, "/moradias").hasAnyRole("ADMIN", "SINDICO");
                request.requestMatchers(HttpMethod.POST, "/moradias").hasAnyRole("ADMIN");
                request.requestMatchers(HttpMethod.PUT, "/moradias/**").hasAnyRole("ADMIN");
                request.requestMatchers(HttpMethod.DELETE, "/moradias/**").hasAnyRole("ADMIN");

                // LOGS
                request.requestMatchers(HttpMethod.GET, "/logs").hasAnyRole("ADMIN");

                request.requestMatchers(HttpMethod.GET, "/avisos").hasAnyRole("SINDICO", "MORADOR", "PORTEIRO", "ADMIN");
                request.requestMatchers(HttpMethod.POST, "/avisos").hasAnyRole("SINDICO", "ADMIN");
                request.requestMatchers(HttpMethod.PUT, "/avisos/**").hasAnyRole("SINDICO", "ADMIN");
                request.requestMatchers(HttpMethod.DELETE, "/avisos/**").hasAnyRole("SINDICO", "ADMIN");


                // Resto das requisições requer autenticação
                request.anyRequest().authenticated();

                })
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();

    }
   
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}