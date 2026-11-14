ALTER TABLE chamado
    CHANGE COLUMN descricao descricao_inicial VARCHAR(300) NOT NULL,
    ADD COLUMN resolucao_chamado VARCHAR(1000) NULL,
    ADD COLUMN id_chamado_anterior INT NULL,
    ADD CONSTRAINT fk_chamado_anterior
        FOREIGN KEY (id_chamado_anterior) REFERENCES chamado (idchamado),
    DROP COLUMN ativo;
