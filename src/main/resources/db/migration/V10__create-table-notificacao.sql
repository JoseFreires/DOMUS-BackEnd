CREATE TABLE notificacao (
    idNotificacao INT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    data_hora DATETIME NOT NULL,
    PRIMARY KEY (idNotificacao),
    INDEX idx_notificacao_usuario_data (id_usuario, data_hora),
    CONSTRAINT fk_notificacao_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario (idUsuario)
        ON DELETE CASCADE
);
