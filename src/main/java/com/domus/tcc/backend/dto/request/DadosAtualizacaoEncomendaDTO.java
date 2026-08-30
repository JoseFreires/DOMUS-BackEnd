package com.domus.tcc.backend.dto.request;

public record DadosAtualizacaoEncomendaDTO(

    String nomePacote,

    String observacao,

    Long idDestinatario,

    String fotoEncomenda
){}