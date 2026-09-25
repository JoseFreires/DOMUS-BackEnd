package com.domus.tcc.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record DadosRegistrarVisitanteDTO(

    @NotNull(message = "O nome do visitante é obrigatório.")
    String nome,
   

    @NotNull(message = "O tipo de visita é obrigatório.")  
    Long idTipoVisita,

    @NotNull(message = "O id da moradia é obrigatório.")
    Long idMoradia

){}