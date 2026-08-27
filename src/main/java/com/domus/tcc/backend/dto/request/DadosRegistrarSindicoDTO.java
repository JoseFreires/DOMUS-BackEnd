package com.domus.tcc.backend.dto.request;

import com.domus.tcc.backend.dto.DadosLoginDTO;

public record DadosRegistrarSindicoDTO(
        DadosRegistrarPessoaDTO pessoa,
        DadosLoginDTO login)
{}
