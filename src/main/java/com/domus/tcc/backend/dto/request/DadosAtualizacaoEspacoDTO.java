package com.domus.tcc.backend.dto.request;

public record DadosAtualizacaoEspacoDTO(

        String nomeEspaco,
        String descricao,
        String capacidadeMaxima,
        Double valorReserva,
        String fotoEspaco,
        String restricao
) {
}
