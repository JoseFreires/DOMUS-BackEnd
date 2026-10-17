package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.Chamado;
import com.domus.tcc.backend.domain.Morador;
import com.domus.tcc.backend.domain.enums.StatusChamado;
import com.domus.tcc.backend.dto.request.DadosAtualizarStatusChamadoDTO;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoChamadoDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarChamado;
import com.domus.tcc.backend.dto.response.DadosConsultaChamadoDTO;
import com.domus.tcc.backend.dto.response.DadosNotificacaoChamadoDTO;
import com.domus.tcc.backend.repository.ChamadoRepository;
import com.domus.tcc.backend.repository.MoradorRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.util.EmailNotificacaoEvent;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
public class ChamadoService {

    private static final Long PAPEL_SINDICO_ID = 4L;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MoradorRepository moradorRepository;

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private FotoService fotoService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public DadosConsultaChamadoDTO registrarChamado(
            DadosRegistrarChamado dados, Usuario logado, MultipartFile arquivo) {

        Usuario usuarioLogado = usuarioRepository.findById(logado.getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado"));

        Morador morador = moradorRepository.findByPessoa_Id(usuarioLogado.getPessoa().getId())
                .orElseThrow(() -> new EntityNotFoundException("Morador associado ao usuário não encontrado"));

        Usuario sindico = usuarioRepository
                .findByPapel_IdPapel(PAPEL_SINDICO_ID)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Não há usuário cadastrado com o papel de síndico (ID 4)"));

        String imagem = null;
        if (arquivo != null && !arquivo.isEmpty()) {
            try {
                imagem = fotoService.uploadFotoChamado(arquivo);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar a imagem do chamado: " + e.getMessage(), e);
            }
        }

        Chamado chamado = chamadoRepository.save(new Chamado(dados, imagem, sindico, morador));
        DadosConsultaChamadoDTO chamadoDTO = new DadosConsultaChamadoDTO(chamado);
        String descricao = morador.getPessoa().getNomeCompleto()
                + " abriu o chamado #" + chamado.getId() + ": " + chamado.getTitulo();
        notificacaoService.registrar(sindico, descricao);
        notificar(sindico, new DadosNotificacaoChamadoDTO(
                "NOVO_CHAMADO",
                descricao,
                chamadoDTO));
        applicationEventPublisher.publishEvent(new EmailNotificacaoEvent(
                sindico.getPessoa().getEmail(),
                "Novo chamado aberto",
                descricao,
                chamado.getDataHoraAbertura()));
        return chamadoDTO;
    }

    @Transactional
    public DadosConsultaChamadoDTO atualizarStatusChamado(
            Long id, DadosAtualizarStatusChamadoDTO dados, UserDetails logado) {

        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado"));

        boolean ehMorador = logado.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_MORADOR".equals(authority.getAuthority()));
        if (ehMorador) {
            if (!(logado instanceof Usuario usuarioMorador)) {
                throw new ResponseStatusException(FORBIDDEN, "Usuário autenticado não é um morador");
            }
            return reabrirChamado(chamado, dados, usuarioMorador);
        }

        if (chamado.getStatusChamado() == dados.statusChamado()) {
            return new DadosConsultaChamadoDTO(chamado);
        }

        chamado.setStatusChamado(dados.statusChamado());
        chamado.setDataHoraFinalizado(
                dados.statusChamado() == StatusChamado.CONCLUIDO
                        ? LocalDateTime.now()
                        : null);
        chamadoRepository.save(chamado);

        Usuario morador = usuarioRepository.findByPessoa_Id(chamado.getMorador().getPessoa().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário associado ao morador que abriu o chamado não encontrado"));
        DadosConsultaChamadoDTO chamadoDTO = new DadosConsultaChamadoDTO(chamado);
        String descricao = "O status do chamado #" + chamado.getId() + " (\""
                + chamado.getTitulo() + "\") foi atualizado para "
                + chamado.getStatusChamado() + ".";
        notificacaoService.registrar(morador, descricao);
        notificar(morador, new DadosNotificacaoChamadoDTO(
                "STATUS_ATUALIZADO",
                descricao,
                chamadoDTO));
        if (dados.statusChamado() == StatusChamado.CONCLUIDO) {
            applicationEventPublisher.publishEvent(new EmailNotificacaoEvent(
                    morador.getPessoa().getEmail(),
                    "Seu chamado foi concluído",
                    descricao,
                    chamado.getDataHoraFinalizado()));
        }
        return chamadoDTO;
    }

    @Transactional
    public DadosConsultaChamadoDTO editarChamado(
            Long id, DadosAtualizacaoChamadoDTO dados, Usuario logado, MultipartFile arquivo) {
        Chamado chamado = buscarChamado(id);
        validarProprietario(chamado, logado);
        validarChamadoAberto(chamado);

        chamado.setTitulo(dados.titulo());
        chamado.setDescricao(dados.descricao());
        chamado.setTipoChamado(dados.tipoChamado());
        if (arquivo != null && !arquivo.isEmpty()) {
            try {
                String imagemAnterior = chamado.getImagem();
                String novaImagem = fotoService.uploadFotoChamado(arquivo);
                chamado.setImagem(novaImagem);
                fotoService.deletarFotoChamado(imagemAnterior);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar a imagem do chamado: " + e.getMessage(), e);
            }
        }
        return new DadosConsultaChamadoDTO(chamadoRepository.save(chamado));
    }

    @Transactional
    public void cancelarChamado(Long id, Usuario logado) {
        Chamado chamado = buscarChamado(id);
        validarProprietario(chamado, logado);
        validarChamadoAberto(chamado);

        chamado.setStatusChamado(StatusChamado.CANCELADO);
        chamado.setDataHoraFinalizado(null);
        chamadoRepository.save(chamado);
        notificarSindicoSobreAcaoMorador(chamado, "cancelou");
    }

    private DadosConsultaChamadoDTO reabrirChamado(
            Chamado chamado, DadosAtualizarStatusChamadoDTO dados, Usuario logado) {
        validarProprietario(chamado, logado);
        if (chamado.getStatusChamado() != StatusChamado.CONCLUIDO
                || dados.statusChamado() != StatusChamado.ABERTO) {
            throw new ResponseStatusException(
                    CONFLICT, "O morador só pode reabrir chamados concluídos");
        }
        if (!StringUtils.hasText(dados.descricao())) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "A descrição com a justificativa para reabrir o chamado é obrigatória");
        }

        chamado.setStatusChamado(StatusChamado.ABERTO);
        chamado.setDataHoraFinalizado(null);
        chamado.setDescricao(dados.descricao().trim());
        Chamado chamadoAtualizado = chamadoRepository.save(chamado);
        notificarSindicoSobreAcaoMorador(chamadoAtualizado, "reabriu",
                "Justificativa: " + chamadoAtualizado.getDescricao());
        return new DadosConsultaChamadoDTO(chamadoAtualizado);
    }

    private Chamado buscarChamado(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado"));
    }

    private void validarProprietario(Chamado chamado, Usuario logado) {
        Usuario usuario = usuarioRepository.findById(logado.getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado"));
        Morador morador = moradorRepository.findByPessoa_Id(usuario.getPessoa().getId())
                .orElseThrow(() -> new EntityNotFoundException("Morador associado ao usuário não encontrado"));
        if (!chamado.getMorador().getId().equals(morador.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Este chamado pertence a outro morador");
        }
    }

    private void validarChamadoAberto(Chamado chamado) {
        if (chamado.getStatusChamado() != StatusChamado.ABERTO) {
            throw new ResponseStatusException(
                    CONFLICT, "Somente chamados com status ABERTO podem ser alterados ou cancelados");
        }
    }

    private void notificarSindicoSobreAcaoMorador(Chamado chamado, String acao) {
        notificarSindicoSobreAcaoMorador(chamado, acao, null);
    }

    private void notificarSindicoSobreAcaoMorador(Chamado chamado, String acao, String complemento) {
        DadosConsultaChamadoDTO chamadoDTO = new DadosConsultaChamadoDTO(chamado);
        String descricao = "O morador " + chamado.getMorador().getPessoa().getNomeCompleto()
                + " " + acao + " o chamado #" + chamado.getId() + " (\""
                + chamado.getTitulo() + "\")."
                + (complemento == null ? "" : " " + complemento);
        notificacaoService.registrar(chamado.getSindico(), descricao);
        notificar(chamado.getSindico(), new DadosNotificacaoChamadoDTO(
                "STATUS_ATUALIZADO",
                descricao,
                chamadoDTO));
    }

    private void notificar(Usuario destinatario, DadosNotificacaoChamadoDTO notificacao) {
        simpMessagingTemplate.convertAndSendToUser(
                destinatario.getUsername(),
                "/queue/notificacoes",
                notificacao);
    }

    @Transactional(readOnly = true)
    public List<DadosConsultaChamadoDTO> listarChamados() {
        return chamadoRepository.findAll()
                .stream()
                .map(DadosConsultaChamadoDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public DadosConsultaChamadoDTO buscarChamadoPorId(Long id) {
        var chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrada"));
        return new DadosConsultaChamadoDTO(chamado);
    }






}
