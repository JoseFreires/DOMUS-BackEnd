package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.response.DadosConsultaLogDTO;
import com.domus.tcc.backend.services.AdmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogsSistemaController {

    @Autowired
    private AdmService admService;

    @GetMapping
    public ResponseEntity<List<DadosConsultaLogDTO>> listarLogsSistema() {
        return ResponseEntity.ok(admService.listarLogsSistema());
    }

}
