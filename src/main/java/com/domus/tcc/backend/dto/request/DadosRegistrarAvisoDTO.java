package com.domus.tcc.backend.dto.request;

import com.domus.tcc.backend.domain.enums.TipoAviso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DadosRegistrarAvisoDTO (

      @NotBlank(message = "O titulo do Aviso é obrigatório")
      String titulo,

      String descricao,

      Boolean prioridade,

      @NotNull(message = "O Tipo do Aviso é obrigatório")
      TipoAviso tipoAviso,

      @NotNull(message = "A data de Validade é obrigatória")
      LocalDate dataValidadeAviso

){




}
