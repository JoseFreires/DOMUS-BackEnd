package com.domus.tcc.backend.util;

public record RedefinicaoSenhaSolicitadaEvent(
        String emailDestino,
        String nomeUsuario,
        String nomeSistema,
        String linkRedefinicao
) {}
