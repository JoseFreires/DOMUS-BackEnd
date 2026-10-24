package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.domain.Chamado;
import com.domus.tcc.backend.domain.enums.StatusChamado;
import com.domus.tcc.backend.domain.enums.TipoChamado;

import java.time.LocalDateTime;

public record DadosConsultaChamadoDTO(
        Long idChamado,
        Long idMorador,
        Long idSindico,
        String titulo,
        String descricao,
        TipoChamado tipoChamado,
        StatusChamado statusChamado,
        String imagem,
        LocalDateTime dataHoraAbertura,
        LocalDateTime dataHoraFinalizado,
        String nomeMorador,
        String nomeSindico,
        String resolucaoChamado
) {
    public DadosConsultaChamadoDTO(Chamado chamado) {
        this(
                chamado.getId(),
                chamado.getMorador().getId(),
                chamado.getSindico().getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getTipoChamado(),
                chamado.getStatusChamado(),
                chamado.getImagem(),
                chamado.getDataHoraAbertura(),
                chamado.getDataHoraFinalizado(),
                chamado.getMorador().getPessoa().getNomeCompleto(),
                chamado.getSindico().getPessoa().getNomeCompleto(),
                chamado.getResolucaoChamado()
        );
    }
}
