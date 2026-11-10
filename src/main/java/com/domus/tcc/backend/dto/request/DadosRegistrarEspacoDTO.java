package com.domus.tcc.backend.dto.request;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosRegistrarEspacoDTO(

        @NotBlank(message = "O nome do espaço é obrigatório")
        @Column(name = "nome")
        String nomeEspaco,

        @Column(name = "descricao")
        String descricao,

        @NotBlank(message = "A capacidade maxima do espaço é obrigatório")
        @Column(name = "capacidade_maxima")
        String capacidadeMaxima,

        @NotNull(message = "O valor do espaço é obrigatório")
        @Column(name = "valor_reserva")
        Double valorReserva,

        @Column(name = "restricao")
        String restricao

) {


}
