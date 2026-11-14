package com.domus.tcc.backend.services;

import com.domus.tcc.backend.infra.aop.LogSistema;
import com.domus.tcc.backend.repository.LogSistemaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LogSistemaServiceTest {

    @ParameterizedTest
    @ValueSource(strings = {"/auth/entrar", "/avisos", "/encomendas"})
    void gravaMetodoStatusEEndpointNoLog(String endpoint) {
        var repository = mock(LogSistemaRepository.class);
        var service = new LogSistemaService(repository);

        service.salvarResposta(null, "POST", endpoint, 200);

        var captor = org.mockito.ArgumentCaptor.forClass(LogSistema.class);
        verify(repository).save(captor.capture());
        assertEquals("POST - 200 OK", captor.getValue().getAcaoRealizada());
        assertEquals(endpoint, captor.getValue().getTabelaAlterada());
        assertNull(captor.getValue().getUsuario());
        assertNull(captor.getValue().getContaAdm());
    }

    @Test
    void falhaDePersistenciaDoLogNaoPropaga() {
        var repository = mock(LogSistemaRepository.class);
        when(repository.save(any(LogSistema.class)))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        var service = new LogSistemaService(repository);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> service.salvarResposta(null, "GET", "/avisos", 403));
    }
}
