package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.Chamado;
import com.domus.tcc.backend.domain.Morador;
import com.domus.tcc.backend.domain.enums.StatusChamado;
import com.domus.tcc.backend.dto.request.DadosAtualizarStatusChamadoDTO;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoChamadoDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarChamado;
import com.domus.tcc.backend.dto.response.DadosConsultaChamadoDTO;
import com.domus.tcc.backend.dto.response.DadosNotificacaoChamadoDTO;
import com.domus.tcc.backend.exception.StatusInvalidoException;
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

        Chamado chamado = new Chamado(dados, imagem, sindico, morador);
        chamado = chamadoRepository.save(chamado);
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
        Chamado chamado = buscarChamado(id);
        return switch (dados.statusChamado()) {
            case TRATANDO -> tratarChamado(chamado, dados, logado);
            case CONCLUIDO -> concluirChamado(chamado, dados, logado);
            case CANCELADO -> cancelarChamado(chamado, dados, logado);
            default -> throw new StatusInvalidoException(
                    "O status solicitado não pode ser definido por este endpoint");
        };
    }

    @Transactional
    public DadosConsultaChamadoDTO editarChamado(
            Long id, DadosAtualizacaoChamadoDTO dados, Usuario logado, MultipartFile arquivo) {
        Chamado chamado = buscarChamado(id);
        validarProprietario(chamado, logado);
        validarChamadoAberto(chamado);

        chamado.setTitulo(dados.titulo());
        chamado.setDescricaoInicial(dados.descricaoInicial());
        chamado.setTipoChamado(dados.tipoChamado());
        if (dados.imagem() != null) {
            chamado.setImagem(dados.imagem());
        }
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

    private DadosConsultaChamadoDTO tratarChamado(
            Chamado chamado, DadosAtualizarStatusChamadoDTO dados, UserDetails logado) {
        validarPapelOperador(logado);
        validarSemResolucao(dados);
        if (chamado.getStatusChamado() != StatusChamado.ABERTO) {
            throw new StatusInvalidoException("Somente chamados ABERTO podem ser tratados");
        }
        chamado.setStatusChamado(StatusChamado.TRATANDO);
        Chamado atualizado = chamadoRepository.save(chamado);
        notificarMoradorSobreAtualizacao(atualizado, false);
        return new DadosConsultaChamadoDTO(atualizado);
    }

    private DadosConsultaChamadoDTO concluirChamado(
            Chamado chamado, DadosAtualizarStatusChamadoDTO dados, UserDetails logado) {
        validarPapelOperador(logado);
        if (chamado.getStatusChamado() != StatusChamado.TRATANDO) {
            throw new StatusInvalidoException("Somente chamados TRATANDO podem ser concluídos");
        }
        if (!StringUtils.hasText(dados.resolucaoChamado())) {
            throw new ResponseStatusException(BAD_REQUEST, "A resolução do chamado é obrigatória");
        }
        chamado.setResolucaoChamado(dados.resolucaoChamado().trim());
        chamado.setDataHoraFinalizado(LocalDateTime.now());
        chamado.setStatusChamado(StatusChamado.CONCLUIDO);
        Chamado atualizado = chamadoRepository.save(chamado);
        notificarMoradorSobreAtualizacao(atualizado, true);
        return new DadosConsultaChamadoDTO(atualizado);
    }

    private DadosConsultaChamadoDTO cancelarChamado(
            Chamado chamado, DadosAtualizarStatusChamadoDTO dados, UserDetails logado) {
        if (!temPapel(logado, "ROLE_MORADOR") || !(logado instanceof Usuario usuarioMorador)) {
            throw new ResponseStatusException(FORBIDDEN, "Somente o morador pode cancelar o chamado");
        }
        validarSemResolucao(dados);
        validarProprietario(chamado, usuarioMorador);
        validarChamadoAberto(chamado);

        chamado.setStatusChamado(StatusChamado.CANCELADO);
        Chamado atualizado = chamadoRepository.save(chamado);
        notificarSindicoSobreAcaoMorador(atualizado, "cancelou");
        return new DadosConsultaChamadoDTO(atualizado);
    }

    private void validarPapelOperador(UserDetails logado) {
        if (!temPapel(logado, "ROLE_SINDICO")) {
            throw new ResponseStatusException(
                    FORBIDDEN, "Somente síndicos podem tratar ou concluir chamados");
        }
    }

    private boolean temPapel(UserDetails logado, String... papeis) {
        return logado.getAuthorities().stream()
                .anyMatch(authority -> List.of(papeis).contains(authority.getAuthority()));
    }

    private void validarSemResolucao(DadosAtualizarStatusChamadoDTO dados) {
        if (StringUtils.hasText(dados.resolucaoChamado())) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "A resolução só deve ser informada ao concluir o chamado");
        }
    }

    private Chamado buscarChamado(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado"));
    }

    private void validarProprietario(Chamado chamado, Usuario logado) {
        Morador morador = buscarMoradorDoUsuario(logado);
        if (!chamado.getMorador().getId().equals(morador.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "Este chamado pertence a outro morador");
        }
    }

    private Morador buscarMoradorDoUsuario(Usuario logado) {
        Usuario usuario = usuarioRepository.findById(logado.getId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado"));
        return moradorRepository.findByPessoa_Id(usuario.getPessoa().getId())
                .orElseThrow(() -> new EntityNotFoundException("Morador associado ao usuário não encontrado"));
    }

    private void validarChamadoAberto(Chamado chamado) {
        if (chamado.getStatusChamado() != StatusChamado.ABERTO) {
            throw new StatusInvalidoException(
                    "Somente chamados com status ABERTO podem ser alterados ou cancelados");
        }
    }

    private void notificarMoradorSobreAtualizacao(Chamado chamado, boolean concluido) {
        Usuario morador = usuarioRepository.findByPessoa_Id(chamado.getMorador().getPessoa().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário associado ao morador que abriu o chamado não encontrado"));
        String descricao = "O status do chamado #" + chamado.getId() + " (\""
                + chamado.getTitulo() + "\") foi atualizado para "
                + chamado.getStatusChamado() + ".";
        DadosConsultaChamadoDTO chamadoDTO = new DadosConsultaChamadoDTO(chamado);
        notificacaoService.registrar(morador, descricao);
        notificar(morador, new DadosNotificacaoChamadoDTO("STATUS_ATUALIZADO", descricao, chamadoDTO));
        if (concluido) {
            applicationEventPublisher.publishEvent(new EmailNotificacaoEvent(
                    morador.getPessoa().getEmail(),
                    "Seu chamado foi concluído",
                    descricao,
                    chamado.getDataHoraFinalizado()));
        }
    }

    private void notificarSindicoSobreAcaoMorador(Chamado chamado, String acao) {
        DadosConsultaChamadoDTO chamadoDTO = new DadosConsultaChamadoDTO(chamado);
        String descricao = "O morador " + chamado.getMorador().getPessoa().getNomeCompleto()
                + " " + acao + " o chamado #" + chamado.getId() + " (\""
                + chamado.getTitulo() + "\").";
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
    public List<DadosConsultaChamadoDTO> listarChamados(StatusChamado status, UserDetails logado) {
        List<Chamado> chamados;
        if (temPapel(logado, "ROLE_MORADOR")) {
            if (!(logado instanceof Usuario usuarioMorador)) {
                throw new ResponseStatusException(FORBIDDEN, "Usuário autenticado não é um morador");
            }
            Long idMorador = buscarMoradorDoUsuario(usuarioMorador).getId();
            chamados = status == null
                    ? chamadoRepository.findAllByMorador_Id(idMorador)
                    : chamadoRepository.findAllByStatusChamadoAndMorador_Id(status, idMorador);
        } else {
            chamados = status == null
                    ? chamadoRepository.findAll()
                    : chamadoRepository.findAllByStatusChamado(status);
        }
        return chamados
                .stream()
                .map(DadosConsultaChamadoDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public DadosConsultaChamadoDTO buscarChamadoPorId(Long id, UserDetails logado) {
        Chamado chamado = buscarChamado(id);
        if (temPapel(logado, "ROLE_MORADOR")) {
            if (!(logado instanceof Usuario usuarioMorador)) {
                throw new ResponseStatusException(FORBIDDEN, "Usuário autenticado não é um morador");
            }
            validarProprietario(chamado, usuarioMorador);
        }
        return new DadosConsultaChamadoDTO(chamado);
    }






}
