USE domus_db;

ALTER TABLE encomenda
  ADD COLUMN nome_recebedor VARCHAR(200) NULL AFTER id_pessoa_destinatario,
  ADD COLUMN id_usuario_porteiro_entrega INT NULL AFTER id_usuario_porteiro,
  ADD CONSTRAINT fk_encomenda_porteiro_entrega
    FOREIGN KEY (id_usuario_porteiro_entrega)
    REFERENCES usuario (idUsuario);