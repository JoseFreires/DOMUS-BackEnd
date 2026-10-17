package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.EspacoCondominial;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoEspacoDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarEspacoDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaEspacoDTO;
import com.domus.tcc.backend.repository.CondominioRepository;
import com.domus.tcc.backend.repository.EspacoCondominialRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;
import com.domus.tcc.backend.security.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
public class EspacoCondominialService {

    @Autowired
    private EspacoCondominialRepository espacoCondominialRepository;

    @Autowired
    private CondominioRepository condominioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FotoService fotoService;


    //GET lista Espaços Condominiais
    @Transactional(readOnly = true)
    public List<DadosConsultaEspacoDTO> listarEspacosCondominiais() {
        return espacoCondominialRepository.findAll()
                .stream()
                .filter(espacoCondominial -> Boolean.TRUE.equals(espacoCondominial.getAtivo()))
                .map(DadosConsultaEspacoDTO::new)
                .toList();
    }

    //GET lista Espaços Condominiais inativos
    @Transactional(readOnly = true)
    public List<DadosConsultaEspacoDTO> listarEspacosCondominiaisInativos() {
        return espacoCondominialRepository.findAll()
                .stream()
                .filter(espacoCondominial -> Boolean.FALSE.equals(espacoCondominial.getAtivo()))
                .map(DadosConsultaEspacoDTO::new)
                .toList();
    }

    //GET Espaço inativo por ID
    @Transactional(readOnly = true)
    public DadosConsultaEspacoDTO buscarEspacoInativoPorId(Long idEspaco) {
        EspacoCondominial espacoCondominial = espacoCondominialRepository.findById(idEspaco)
                .filter(espaco -> Boolean.FALSE.equals(espaco.getAtivo()))
                .orElseThrow(() -> new EntityNotFoundException("Espaço inativo não encontrado"));

        return new DadosConsultaEspacoDTO(espacoCondominial);
    }

    //GET por id
    @Transactional(readOnly = true)
    public DadosConsultaEspacoDTO buscarEspacoId(Long idEspaco) {
        EspacoCondominial espacoCondominial = espacoCondominialRepository.findById(idEspaco)
                .orElseThrow(() -> new EntityNotFoundException("Espaço não encontrado"));

        return new DadosConsultaEspacoDTO(espacoCondominial);
    }

    //POST Espaços Condominiais
    @PreAuthorize("hasRole('SINDICO')")
    @Transactional
    public DadosConsultaEspacoDTO registrarEspacoCondominial(DadosRegistrarEspacoDTO dados, Usuario logado, MultipartFile arquivo) {


        var sindico = usuarioRepository.findById(logado.getId())
                .orElseThrow(() -> new EntityNotFoundException("Sindico não encontrado"));


        var condominio = condominioRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("Condominio não encontrado"));

        String fotoUrl = null;
        if (arquivo != null && !arquivo.isEmpty()) {
            try {
                fotoUrl = fotoService.uploadEspacoCondominial(arquivo);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar foto do aviso: " + e.getMessage(), e);
            }
        }

        var espacoCondominial = new EspacoCondominial(dados, fotoUrl, sindico, condominio);
        espacoCondominialRepository.save(espacoCondominial);

        return new DadosConsultaEspacoDTO(espacoCondominial);
    }

    //PUT Espaços
    @Transactional
    public DadosConsultaEspacoDTO editarEspacoCondominial(Long id, DadosAtualizacaoEspacoDTO dados, MultipartFile novoArquivo) {

        EspacoCondominial espacoCondominial = espacoCondominialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Espaço Condominial não encontrado"));

        if (dados.capacidadeMaxima() != null) {
            espacoCondominial.setCapacidadeMaxima(dados.capacidadeMaxima());
        }

        if (dados.descricao() != null) {
            espacoCondominial.setDescricao(dados.descricao());
        }

        if (dados.nomeEspaco() != null) {
            espacoCondominial.setNomeEspaco(dados.nomeEspaco());
        }

        if (dados.valorReserva() != null) {
            espacoCondominial.setValorReserva(dados.valorReserva());
        }

        if (dados.restricao() != null) {
            espacoCondominial.setRestricao(dados.restricao());
        }

        if (novoArquivo != null && !novoArquivo.isEmpty()) {
            try {

                if (espacoCondominial.getFotoEspaco() != null && !espacoCondominial.getFotoEspaco().isBlank()) {
                    fotoService.deletarEspacoCondominial(espacoCondominial.getFotoEspaco());
                }

                String novaFotoUrl = fotoService.uploadEspacoCondominial(novoArquivo);
                espacoCondominial.setFotoEspaco(novaFotoUrl);

            } catch (Exception e) {
                throw new RuntimeException("Erro ao atualizar a foto do aviso: " + e.getMessage(), e);
            }
        }

        return new DadosConsultaEspacoDTO(espacoCondominial);

    }

    //PATCH Reativar espaco
    @Transactional
    public DadosConsultaEspacoDTO reativarEspaco(Long idEspaco){

        EspacoCondominial espacoCondominial = espacoCondominialRepository.findById(idEspaco)
                .orElseThrow(() -> new EntityNotFoundException("Espaço Condominial não encontrado!"));

        espacoCondominial.setAtivo(true);

        return new DadosConsultaEspacoDTO(espacoCondominial);
    }


    //Soft Delete
    @Transactional
    public void desativarEspacos(Long id){

        EspacoCondominial espacoCondominial = espacoCondominialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Espaço Condominial não encontrado."));

        espacoCondominial.setAtivo(false);

        espacoCondominialRepository.save(espacoCondominial);
    }
}
