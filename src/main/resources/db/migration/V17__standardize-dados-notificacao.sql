ALTER TABLE notificacao
    CHANGE COLUMN data_hora data_hora_chegada
        DATETIME NOT NULL,
    MODIFY COLUMN descricao
        VARCHAR(300) NOT NULL;