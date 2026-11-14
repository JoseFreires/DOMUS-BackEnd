package com.domus.tcc.backend.security;

public final class RotasPublicas {

    public static final String ENTRAR = "/auth/entrar";
    public static final String SAIR = "/auth/sair";
    public static final String USUARIO_ATUAL = "/auth/eu";
    public static final String ESQUECI_SENHA = "/auth/esqueci-minha-senha";
    public static final String REDEFINIR_SENHA = "/auth/redefinir-senha";
    public static final String ACEITAR_TERMOS = "/auth/aceitar-termos";
    public static final String SWAGGER = "/swagger-ui.html";
    public static final String SWAGGER_PREFIX = "/swagger-ui";
    public static final String SWAGGER_ASSETS = SWAGGER_PREFIX + "/**";
    public static final String OPENAPI_PREFIX = "/v3/api-docs";
    public static final String OPENAPI = OPENAPI_PREFIX + "/**";

    private RotasPublicas() {
    }
}
