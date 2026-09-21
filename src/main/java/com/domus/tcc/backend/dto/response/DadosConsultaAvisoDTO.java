package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.domain.AvisoCondominial;
import com.domus.tcc.backend.domain.Condominio;
import com.domus.tcc.backend.domain.enums.TipoAviso;
import com.domus.tcc.backend.security.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DadosConsultaAvisoDTO(

    Long idAviso,

    String titulo,

    String descricao,

    String fotoAviso,

    LocalDateTime dataHoraAviso,

    Boolean prioridade,

    TipoAviso tipoAviso,

    LocalDate dataValidadeAviso,

    Boolean ativo,

    String nomeCondominio,

    String nomeSindico

) {

    public DadosConsultaAvisoDTO(AvisoCondominial avisoCondominial){
        this(

                avisoCondominial.getId(),
                avisoCondominial.getTitulo(),
                avisoCondominial.getDescricao(),
                avisoCondominial.getFotoAviso(),
                avisoCondominial.getDataHoraAviso(),
                avisoCondominial.getPrioridade(),
                avisoCondominial.getTipoAviso(),
                avisoCondominial.getDataValidadeAviso(),
                avisoCondominial.getAtivo(),
                avisoCondominial.getCondominio().getNome_condominio(),
                avisoCondominial.getSindico().getPessoa().getNomeCompleto()


        );
    }
}
