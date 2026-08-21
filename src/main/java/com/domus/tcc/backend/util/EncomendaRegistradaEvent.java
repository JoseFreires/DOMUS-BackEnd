package com.domus.tcc.backend.util;


public record EncomendaRegistradaEvent(String nomeDestinatario,
                                       String emailDestinatario,
                                       String nomePacote,
                                       String nomePorteiro,
                                       String dataChegada) {
}
