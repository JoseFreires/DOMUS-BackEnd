package com.domus.tcc.backend.util;

import com.domus.tcc.backend.services.AvisoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class AvisoExpericaoDate {

    private static final Logger log = LoggerFactory.getLogger(AvisoExpericaoDate.class);

    private final AvisoService avisoService;

    public AvisoExpericaoDate(AvisoService avisoService) {
        this.avisoService = avisoService;
    }

    @Scheduled(cron = "0 1 0 * * *")
    public void executar() {
        int quantidade = avisoService.expirarAvisosVencidos();
        if (quantidade > 0) {
            log.info("{} aviso(s) expirado(s) automaticamente", quantidade);
        }
    }

}
