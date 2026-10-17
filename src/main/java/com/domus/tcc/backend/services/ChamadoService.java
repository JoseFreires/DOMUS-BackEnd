package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.Chamado;
import com.domus.tcc.backend.domain.Morador;
import com.domus.tcc.backend.domain.enums.StatusChamado;
import com.domus.tcc.backend.dto.request.DadosAtualizarStatusChamadoDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

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
                imagem = fotoService.uploadFotoAviso(arquivo);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar a imagem do chamado: " + e.getMessage(), e);
            }
        }

        Chamado chamado = chamadoRepository.save(new Chamado(dados, imagem, sindico, morador));
        DadosConsultaChamadoDTO chamadoDTO = new DadosConsultaChamadoDTO(chamado);
        String descricao = morador.getPessoa().getNomeCompleto()
                + " abriu um chamado: " + chamado.getTitulo();
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
            Long id, DadosAtualizarStatusChamadoDTO dados) {

        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado"));

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
        String descricao = "O status do chamado \"" + chamado.getTitulo()
                + "\" foi atualizado para " + chamado.getStatusChamado() + ".";
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

    private void notificar(Usuario destinatario, DadosNotificacaoChamadoDTO notificacao) {
        simpMessagingTemplate.convertAndSendToUser(
                destinatario.getUsername(),
                "/queue/notificacoes",
                notificacao);
    }


}
