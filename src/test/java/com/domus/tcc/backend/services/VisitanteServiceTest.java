package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.Bloco;
import com.domus.tcc.backend.domain.Moradia;
import com.domus.tcc.backend.domain.Pessoa;
import com.domus.tcc.backend.domain.TipoVisita;
import com.domus.tcc.backend.domain.Visitante;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoVisitanteDTO;
import com.domus.tcc.backend.repository.MoradiaRepository;
import com.domus.tcc.backend.repository.TipoVisitaRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;
import com.domus.tcc.backend.repository.VisitanteRepository;
import com.domus.tcc.backend.security.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitanteServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VisitanteRepository visitanteRepository;

    @Mock
    private MoradiaRepository moradiaRepository;

    @Mock
    private TipoVisitaRepository tipoVisitaRepository;

    @InjectMocks
    private VisitanteService visitanteService;

    @Test
    void deveAtualizarNomeTipoVisitaEMoradiaSemAlterarChegadaOuPorteiro() {
        LocalDateTime chegada = LocalDateTime.now().minusHours(1);
        Usuario porteiro = new Usuario();
        porteiro.setId(5L);
        Pessoa pessoaPorteiro = new Pessoa();
        pessoaPorteiro.setNomeCompleto("Porteiro");
        porteiro.setPessoa(pessoaPorteiro);

        TipoVisita tipoAnterior = tipoVisita(1L, "Entrega");
        TipoVisita tipoNovo = tipoVisita(2L, "Prestador");
        Moradia moradiaAnterior = moradia(10L, "101", "Bloco A");
        Moradia moradiaNova = moradia(11L, "202", "Bloco B");
        Visitante visitante = new Visitante();
        visitante.setId(100L);
        visitante.setNome("Nome incorreto");
        visitante.setDataHoraChegada(chegada);
        visitante.setTipoVisita(tipoAnterior);
        visitante.setPorteiro(porteiro);
        visitante.setMoradias(new ArrayList<>(java.util.List.of(moradiaAnterior)));

        when(visitanteRepository.findById(100L)).thenReturn(Optional.of(visitante));
        when(moradiaRepository.findById(11L)).thenReturn(Optional.of(moradiaNova));
        when(tipoVisitaRepository.findById(2L)).thenReturn(Optional.of(tipoNovo));
        when(visitanteRepository.save(visitante)).thenReturn(visitante);

        var resultado = visitanteService.atualizarVisitante(
                100L, new DadosAtualizacaoVisitanteDTO("Nome corrigido", 2L, 11L));

        assertEquals("Nome corrigido", resultado.nome());
        assertEquals(2L, resultado.idTipoVisita());
        assertEquals(11L, resultado.idMoradia());
        assertEquals(chegada.toString(), resultado.dataHoraChegada());
        assertEquals(5L, resultado.idPorteiro());
        assertEquals("Nome corrigido", visitante.getNome());
        assertEquals(tipoNovo, visitante.getTipoVisita());
        assertEquals(java.util.List.of(moradiaAnterior, moradiaNova), visitante.getMoradias());
        assertEquals(chegada, visitante.getDataHoraChegada());
        assertEquals(porteiro, visitante.getPorteiro());
        verify(visitanteRepository).save(visitante);
    }

    @Test
    void deveRetornarErroQuandoVisitanteNaoExiste() {
        when(visitanteRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> visitanteService.atualizarVisitante(
                        100L, new DadosAtualizacaoVisitanteDTO("Nome", 2L, 11L)));
    }

    @Test
    void deveAtualizarSomenteNomePreservandoTipoEMoradia() {
        Usuario porteiro = new Usuario();
        porteiro.setId(5L);
        Pessoa pessoaPorteiro = new Pessoa();
        pessoaPorteiro.setNomeCompleto("Porteiro");
        porteiro.setPessoa(pessoaPorteiro);
        TipoVisita tipo = tipoVisita(1L, "Entrega");
        Moradia moradia = moradia(10L, "101", "Bloco A");
        Visitante visitante = new Visitante();
        visitante.setId(100L);
        visitante.setNome("Nome anterior");
        visitante.setDataHoraChegada(LocalDateTime.now());
        visitante.setPorteiro(porteiro);
        visitante.setTipoVisita(tipo);
        visitante.setMoradias(new ArrayList<>(java.util.List.of(moradia)));
        when(visitanteRepository.findById(100L)).thenReturn(Optional.of(visitante));
        when(visitanteRepository.save(visitante)).thenReturn(visitante);

        var resultado = visitanteService.atualizarVisitante(
                100L, new DadosAtualizacaoVisitanteDTO("Nome corrigido", null, null));

        assertEquals("Nome corrigido", resultado.nome());
        assertEquals(1L, resultado.idTipoVisita());
        assertEquals(10L, resultado.idMoradia());
        assertEquals(tipo, visitante.getTipoVisita());
        assertEquals(java.util.List.of(moradia), visitante.getMoradias());
        verify(visitanteRepository).save(visitante);
    }

    @Test
    void deveRejeitarAtualizacaoSemCampos() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> visitanteService.atualizarVisitante(
                        100L, new DadosAtualizacaoVisitanteDTO(null, null, null)));

        assertEquals(400, exception.getStatusCode().value());
    }

    private TipoVisita tipoVisita(Long id, String nome) {
        TipoVisita tipo = new TipoVisita();
        tipo.setId(id);
        tipo.setNomeVisita(nome);
        return tipo;
    }

    private Moradia moradia(Long id, String numero, String nomeBloco) {
        Bloco bloco = new Bloco();
        bloco.setNome_torre(nomeBloco);
        Moradia moradia = new Moradia();
        moradia.setId(id);
        moradia.setNumero(numero);
        moradia.setBloco(bloco);
        return moradia;
    }
}
