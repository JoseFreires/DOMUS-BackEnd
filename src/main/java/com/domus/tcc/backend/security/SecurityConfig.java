package com.domus.tcc.backend.security;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        request -> "/auth/eu/perfil".equals(request.getServletPath())
                ))
                .authorizeHttpRequests(request -> {
                    request.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
                    request.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    request.requestMatchers(HttpMethod.POST, "/auth/entrar").permitAll();
                    request.requestMatchers(HttpMethod.POST, "/auth/sair").permitAll();
                    request.requestMatchers(HttpMethod.GET, "/auth/eu").permitAll();
                    request.requestMatchers(HttpMethod.GET, "/auth/eu/perfil").hasAnyRole("MORADOR", "SINDICO", "PORTEIRO", "ADMIN");
                    request.requestMatchers(HttpMethod.PUT, "/auth/eu/perfil").hasAnyRole("MORADOR", "SINDICO", "PORTEIRO");
                    request.requestMatchers(HttpMethod.DELETE, "/auth/eu/perfil").hasAnyRole("MORADOR", "SINDICO", "PORTEIRO");
                    request.requestMatchers(HttpMethod.POST, "/auth/esqueci-minha-senha").permitAll();
                    request.requestMatchers(HttpMethod.PUT, "/auth/redefinir-senha").permitAll();
                    request.requestMatchers(HttpMethod.PUT, "/auth/aceitar-termos").permitAll();
                    request.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll();

                    request.requestMatchers(HttpMethod.POST, "/chamados").hasRole("MORADOR");
                    request.requestMatchers(HttpMethod.PUT, "/chamados/*").hasRole("MORADOR");
                    request.requestMatchers(HttpMethod.DELETE, "/chamados/*").hasRole("MORADOR");
                    request.requestMatchers(HttpMethod.PATCH, "/chamados/*/status").hasAnyRole("MORADOR", "SINDICO", "ADMIN");

                    request.requestMatchers(HttpMethod.POST, "/encomendas").hasAnyRole("PORTEIRO", "ADMIN");
                    request.requestMatchers(HttpMethod.GET, "/encomendas").hasAnyRole("PORTEIRO", "MORADOR", "SINDICO", "ADMIN");
                    request.requestMatchers(HttpMethod.PUT, "/encomendas/**").hasAnyRole("PORTEIRO", "ADMIN");
                    request.requestMatchers(HttpMethod.PATCH, "/encomendas/**").hasAnyRole("PORTEIRO", "ADMIN");
                    request.requestMatchers(HttpMethod.DELETE, "/encomendas/**").hasAnyRole("PORTEIRO", "ADMIN");

                    request.requestMatchers(HttpMethod.GET,
                                    "/moradores/inativos/**",
                                    "/porteiros/inativos/**",
                                    "/sindicos/inativos/**",
                                    "/espacos/inativos/**")
                            .hasAnyRole("ADMIN", "SINDICO");
                    request.requestMatchers(HttpMethod.PATCH,
                                    "/moradores/inativos/**",
                                    "/porteiros/inativos/**",
                                    "/sindicos/inativos/**",
                                    "/espacos/inativos/**")
                            .hasAnyRole("ADMIN", "SINDICO");

                    request.requestMatchers(HttpMethod.POST, "/moradores").hasAnyRole("SINDICO", "ADMIN");
                    request.requestMatchers(HttpMethod.GET, "/moradores").hasAnyRole("SINDICO", "ADMIN", "PORTEIRO");
                    request.requestMatchers(HttpMethod.GET, "/moradores/**").hasAnyRole("MORADOR", "SINDICO", "ADMIN", "PORTEIRO");
                    request.requestMatchers(HttpMethod.PUT, "/moradores/**").hasAnyRole("MORADOR", "SINDICO", "ADMIN");
                    request.requestMatchers(HttpMethod.DELETE, "/moradores/**").hasAnyRole("ADMIN", "SINDICO");

                    request.requestMatchers(HttpMethod.POST, "/pessoas-autorizadas").hasAnyRole("MORADOR");
                    request.requestMatchers(HttpMethod.GET, "/pessoas-autorizadas").hasAnyRole("MORADOR", "PORTEIRO", "ADMIN");
                    request.requestMatchers(HttpMethod.PUT, "/pessoas-autorizadas/**").hasAnyRole("MORADOR");
                    request.requestMatchers(HttpMethod.DELETE, "/pessoas-autorizadas/**").hasAnyRole("MORADOR");

                    request.requestMatchers(HttpMethod.GET, "/condominios").hasAnyRole("ADMIN", "SINDICO");
                    request.requestMatchers(HttpMethod.POST, "/condominios").hasAnyRole("ADMIN");
                    request.requestMatchers(HttpMethod.PUT, "/condominios/**").hasAnyRole("ADMIN");
                    request.requestMatchers(HttpMethod.DELETE, "/condominios/**").hasAnyRole("ADMIN");

                    request.requestMatchers(HttpMethod.GET, "/blocos").hasAnyRole("ADMIN", "SINDICO");
                    request.requestMatchers(HttpMethod.POST, "/blocos").hasAnyRole("ADMIN");
                    request.requestMatchers(HttpMethod.PUT, "/blocos/**").hasAnyRole("ADMIN");
                    request.requestMatchers(HttpMethod.DELETE, "/blocos/**").hasAnyRole("ADMIN");

                    request.requestMatchers(HttpMethod.GET, "/moradias").hasAnyRole("ADMIN", "SINDICO");
                    request.requestMatchers(HttpMethod.POST, "/moradias").hasAnyRole("ADMIN");
                    request.requestMatchers(HttpMethod.PUT, "/moradias/**").hasAnyRole("ADMIN");
                    request.requestMatchers(HttpMethod.DELETE, "/moradias/**").hasAnyRole("ADMIN");

                    request.requestMatchers(HttpMethod.POST, "/espacos").hasAnyRole("SINDICO", "ADMIN");
                    request.requestMatchers(HttpMethod.GET, "/espacos").hasAnyRole("SINDICO", "ADMIN", "PORTEIRO", "MORADOR");
                    request.requestMatchers(HttpMethod.GET, "/espacos/**").hasAnyRole("MORADOR", "SINDICO", "ADMIN", "PORTEIRO");
                    request.requestMatchers(HttpMethod.PUT, "/espacos/**").hasAnyRole("SINDICO", "ADMIN");
                    request.requestMatchers(HttpMethod.DELETE, "/espacos/**").hasAnyRole("ADMIN", "SINDICO");

                    request.requestMatchers(HttpMethod.GET, "/logs").hasAnyRole("ADMIN");
                    request.requestMatchers(HttpMethod.GET, "/avisos").hasAnyRole("SINDICO", "MORADOR", "PORTEIRO", "ADMIN");
                    request.requestMatchers(HttpMethod.POST, "/avisos").hasAnyRole("SINDICO", "ADMIN");
                    request.requestMatchers(HttpMethod.PUT, "/avisos/**").hasAnyRole("SINDICO", "ADMIN");
                    request.requestMatchers(HttpMethod.DELETE, "/avisos/**").hasAnyRole("SINDICO", "ADMIN");

                    request.anyRequest().authenticated();
                })
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}