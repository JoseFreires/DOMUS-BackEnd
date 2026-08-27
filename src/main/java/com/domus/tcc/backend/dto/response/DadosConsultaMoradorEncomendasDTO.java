package com.domus.tcc.backend.dto.response;

import java.util.List;

public record DadosConsultaMoradorEncomendasDTO (
        List<DadosConsultaEncomendaDTO> encomendas
) {

}
