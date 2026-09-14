package com.domus.tcc.backend.util;

import java.util.List;

public record AvisoRegistradoEvent(
                                List<String> emailsDestinatarios,
                                String tituloAviso,
                                String nomeSindico,
                                String dataAbertura,
                                String dataValidade
) {}