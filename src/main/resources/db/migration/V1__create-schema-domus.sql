-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema domus_db
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema domus_db
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `domus_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `domus_db` ;

-- -----------------------------------------------------
-- Table `domus_db`.`condominio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`condominio` (
  `idCondominio` INT NOT NULL AUTO_INCREMENT,
  `nome_condominio` VARCHAR(120) NOT NULL,
  PRIMARY KEY (`idCondominio`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`bloco`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`bloco` (
  `idBloco` INT NOT NULL AUTO_INCREMENT,
  `nome_torre` VARCHAR(45) NOT NULL,
  `condominio_idCondominio` INT NOT NULL,
  PRIMARY KEY (`idBloco`),
  INDEX `fk_bloco_condominio` (`condominio_idCondominio` ASC) VISIBLE,
  CONSTRAINT `fk_bloco_condominio`
    FOREIGN KEY (`condominio_idCondominio`)
    REFERENCES `domus_db`.`condominio` (`idCondominio`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`conta_adm`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`conta_adm` (
  `idConta` INT NOT NULL AUTO_INCREMENT,
  `nome_conta` VARCHAR(45) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `senha` VARCHAR(100) NOT NULL,
  `ativo` TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`idConta`),
  UNIQUE INDEX `nome_conta_UNIQUE` (`nome_conta` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`pessoa`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`pessoa` (
  `idPessoa` INT NOT NULL AUTO_INCREMENT,
  `nome_completo` VARCHAR(200) NOT NULL,
  `cpf` VARCHAR(11) NOT NULL,
  `email` VARCHAR(150) NOT NULL,
  `foto_perfil` VARCHAR(255) NULL,
  `ativo` TINYINT(1) NOT NULL DEFAULT '1',
  `telefone` VARCHAR(14) NOT NULL,
  `data_nascimento` DATE NOT NULL,
  PRIMARY KEY (`idPessoa`),
  UNIQUE INDEX `cpf_UNIQUE` (`cpf` ASC) VISIBLE,
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`papel`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`papel` (
  `idPapel` INT NOT NULL AUTO_INCREMENT,
  `nome_papel` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`idPapel`),
  UNIQUE INDEX `nome_papel_UNIQUE` (`nome_papel` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 5
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`usuario`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`usuario` (
  `idUsuario` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(100) NOT NULL,
  `senha` VARCHAR(255) NOT NULL,
  `Pessoa_idPessoa` INT NOT NULL,
  `papel_idPapel` INT NOT NULL,
  PRIMARY KEY (`idUsuario`),
  UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE,
  INDEX `fk_usuario_pessoa_idx` (`Pessoa_idPessoa` ASC) VISIBLE,
  INDEX `fk_usuario_papel1_idx` (`papel_idPapel` ASC) VISIBLE,
  CONSTRAINT `fk_usuario_pessoa`
    FOREIGN KEY (`Pessoa_idPessoa`)
    REFERENCES `domus_db`.`pessoa` (`idPessoa`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_usuario_papel1`
    FOREIGN KEY (`papel_idPapel`)
    REFERENCES `domus_db`.`papel` (`idPapel`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`encomenda`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`encomenda` (
  `idEncomenda` INT NOT NULL AUTO_INCREMENT,
  `nome_pacote` VARCHAR(120) NOT NULL,
  `data_hora_recebido` DATETIME NOT NULL,
  `data_hora_retirado` DATETIME NULL DEFAULT NULL,
  `foto_encomenda` VARCHAR(255) NOT NULL,
  `status` ENUM('RECEBIDA', 'ENTREGUE') NOT NULL,
  `token` VARCHAR(45) NOT NULL,
  `observacao` VARCHAR(500) NULL DEFAULT NULL,
  `tipo_retirada` ENUM('TERCEIRO', 'MORADOR', 'AUTORIZADA') NULL DEFAULT NULL,
  `id_usuario_porteiro` INT NOT NULL,
  `id_pessoa_destinatario` INT NOT NULL,
  PRIMARY KEY (`idEncomenda`),
  INDEX `fk_encomenda_porteiro` (`id_usuario_porteiro` ASC) VISIBLE,
  INDEX `fk_encomenda_pessoa` (`id_pessoa_destinatario` ASC) VISIBLE,
  CONSTRAINT `fk_encomenda_pessoa`
    FOREIGN KEY (`id_pessoa_destinatario`)
    REFERENCES `domus_db`.`pessoa` (`idPessoa`),
  CONSTRAINT `fk_encomenda_porteiro`
    FOREIGN KEY (`id_usuario_porteiro`)
    REFERENCES `domus_db`.`usuario` (`idUsuario`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`flyway_schema_history`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`flyway_schema_history` (
  `installed_rank` INT NOT NULL,
  `version` VARCHAR(50) NULL DEFAULT NULL,
  `description` VARCHAR(200) NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `script` VARCHAR(1000) NOT NULL,
  `checksum` INT NULL DEFAULT NULL,
  `installed_by` VARCHAR(100) NOT NULL,
  `installed_on` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` INT NOT NULL,
  `success` TINYINT(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  INDEX `flyway_schema_history_s_idx` (`success` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`historico_sindico`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`historico_sindico` (
  `idHistorico` INT NOT NULL AUTO_INCREMENT,
  `data_inicio` DATE NOT NULL,
  `data_fim` DATE NULL DEFAULT NULL,
  `Pessoa_idPessoa` INT NOT NULL,
  PRIMARY KEY (`idHistorico`),
  INDEX `fk_historico_sindico_pessoa_idx` (`Pessoa_idPessoa` ASC) VISIBLE,
  CONSTRAINT `fk_historico_sindico_pessoa`
    FOREIGN KEY (`Pessoa_idPessoa`)
    REFERENCES `domus_db`.`pessoa` (`idPessoa`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`log_sistema`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`log_sistema` (
  `idLog` INT NOT NULL AUTO_INCREMENT,
  `acao_realizada` VARCHAR(255) NOT NULL,
  `tabela_alterada` VARCHAR(45) NULL DEFAULT NULL,
  `data_hora` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  `Usuario_idUsuario` INT NULL DEFAULT NULL,
  `conta_adm_idConta` INT NULL DEFAULT NULL,
  PRIMARY KEY (`idLog`),
  INDEX `fk_log_usuario` (`Usuario_idUsuario` ASC) VISIBLE,
  INDEX `fk_log_svc` (`conta_adm_idConta` ASC) VISIBLE,
  CONSTRAINT `fk_log_svc`
    FOREIGN KEY (`conta_adm_idConta`)
    REFERENCES `domus_db`.`conta_adm` (`idConta`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_log_usuario`
    FOREIGN KEY (`Usuario_idUsuario`)
    REFERENCES `domus_db`.`usuario` (`idUsuario`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`moradia`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`moradia` (
  `idMoradia` INT NOT NULL AUTO_INCREMENT,
  `numero` VARCHAR(20) NOT NULL,
  `bloco_idBloco` INT NOT NULL,
  PRIMARY KEY (`idMoradia`),
  INDEX `fk_moradia_bloco` (`bloco_idBloco` ASC) VISIBLE,
  CONSTRAINT `fk_moradia_bloco`
    FOREIGN KEY (`bloco_idBloco`)
    REFERENCES `domus_db`.`bloco` (`idBloco`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`morador`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`morador` (
  `idMorador` INT NOT NULL AUTO_INCREMENT,
  `data_chegada` DATE NOT NULL,
  `data_saida` DATE NULL DEFAULT NULL,
  `pessoa_idPessoa` INT NOT NULL,
  `moradia_idMoradia` INT NOT NULL,
  PRIMARY KEY (`idMorador`),
  INDEX `fk_morador_pessoa1_idx` (`pessoa_idPessoa` ASC) VISIBLE,
  INDEX `fk_morador_moradia1_idx` (`moradia_idMoradia` ASC) VISIBLE,
  CONSTRAINT `fk_morador_pessoa1`
    FOREIGN KEY (`pessoa_idPessoa`)
    REFERENCES `domus_db`.`pessoa` (`idPessoa`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_morador_moradia1`
    FOREIGN KEY (`moradia_idMoradia`)
    REFERENCES `domus_db`.`moradia` (`idMoradia`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`porteiro`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`porteiro` (
  `idPorteiro` INT NOT NULL AUTO_INCREMENT,
  `turno` ENUM('MANHA', 'TARDE', 'NOITE') NOT NULL,
  `empresa_responsavel` VARCHAR(45) NOT NULL,
  `pessoa_idPessoa` INT NOT NULL,
  PRIMARY KEY (`idPorteiro`),
  INDEX `fk_porteiro_pessoa1_idx` (`pessoa_idPessoa` ASC) VISIBLE,
  CONSTRAINT `fk_porteiro_pessoa1`
    FOREIGN KEY (`pessoa_idPessoa`)
    REFERENCES `domus_db`.`pessoa` (`idPessoa`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`pessoa_autorizada`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`pessoa_autorizada` (
  `idPesssoaAutorizada` INT NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(100) NOT NULL,
  `cpf` VARCHAR(11) NOT NULL,
  `morador_idMorador` INT NOT NULL,
  PRIMARY KEY (`idPesssoaAutorizada`),
  INDEX `fk_pessoa_autorizada_morador1_idx` (`morador_idMorador` ASC) VISIBLE,
  CONSTRAINT `fk_pessoa_autorizada_morador1`
    FOREIGN KEY (`morador_idMorador`)
    REFERENCES `domus_db`.`morador` (`idMorador`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `domus_db`.`tipo_visita`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`tipo_visita` (
  `idtTipo_visita` INT NOT NULL AUTO_INCREMENT,
  `nome_visita` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`idtTipo_visita`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `domus_db`.`convidado`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`convidado` (
  `idConvidado` INT NOT NULL AUTO_INCREMENT,
  `observacao` VARCHAR(255) NULL,
  `recorrencia` ENUM('LIVRE', 'UNICA', 'DIARIA', 'SEMANAL', 'MENSAL') NOT NULL,
  `nome_convidado` VARCHAR(45) NOT NULL,
  `cpf` VARCHAR(11) NOT NULL,
  `telefone` VARCHAR(11) NOT NULL,
  `morador_idMorador` INT NOT NULL,
  `tipo_visita_idtTipo_visita` INT NOT NULL,
  PRIMARY KEY (`idConvidado`),
  INDEX `fk_convidado_morador1_idx` (`morador_idMorador` ASC) VISIBLE,
  INDEX `fk_convidado_tipo_visita1_idx` (`tipo_visita_idtTipo_visita` ASC) VISIBLE,
  CONSTRAINT `fk_convidado_morador1`
    FOREIGN KEY (`morador_idMorador`)
    REFERENCES `domus_db`.`morador` (`idMorador`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_convidado_tipo_visita1`
    FOREIGN KEY (`tipo_visita_idtTipo_visita`)
    REFERENCES `domus_db`.`tipo_visita` (`idtTipo_visita`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `domus_db`.`log_visita`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`log_visita` (
  `idLog_visita` INT NOT NULL AUTO_INCREMENT,
  `data_hora_chegada` DATETIME NOT NULL,
  `data_hora_saida` DATETIME NULL,
  `convidado_idconvidado` INT NOT NULL,
  `id_usuario_porteiro` INT NOT NULL,
  PRIMARY KEY (`idLog_visita`),
  INDEX `fk_log_visita_convidado1_idx` (`convidado_idconvidado` ASC) VISIBLE,
  INDEX `fk_log_visita_usuario1_idx` (`id_usuario_porteiro` ASC) VISIBLE,
  CONSTRAINT `fk_log_visita_convidado1`
    FOREIGN KEY (`convidado_idconvidado`)
    REFERENCES `domus_db`.`convidado` (`idConvidado`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_log_visita_usuario1`
    FOREIGN KEY (`id_usuario_porteiro`)
    REFERENCES `domus_db`.`usuario` (`idUsuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `domus_db`.`chamado`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`chamado` (
  `idchamado` INT NOT NULL AUTO_INCREMENT,
  `titulo` VARCHAR(45) NOT NULL,
  `descricao` VARCHAR(300) NOT NULL,
  `tipo_chamado` ENUM('RECLAMACAO', 'DUVIDAS', 'SUGESTAO', 'INCIDENTE', 'OUTROS') NOT NULL,
  `status_chamado` ENUM('ABERTO', 'TRATANDO', 'CONCLUIDO', 'CANCELADO') NOT NULL,
  `imagem` VARCHAR(255) NULL,
  `data_hora_abertura` DATETIME NOT NULL,
  `data_hora_finalizado` DATETIME NULL,
  `ativo` TINYINT(1) NOT NULL,
  `morador_idMorador` INT NOT NULL,
  `id_usuario_sindico` INT NOT NULL,
  PRIMARY KEY (`idchamado`),
  INDEX `fk_chamado_morador1_idx` (`morador_idMorador` ASC) VISIBLE,
  INDEX `fk_chamado_usuario1_idx` (`id_usuario_sindico` ASC) VISIBLE,
  CONSTRAINT `fk_chamado_morador1`
    FOREIGN KEY (`morador_idMorador`)
    REFERENCES `domus_db`.`morador` (`idMorador`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_chamado_usuario1`
    FOREIGN KEY (`id_usuario_sindico`)
    REFERENCES `domus_db`.`usuario` (`idUsuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `domus_db`.`visitante`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`visitante` (
  `idVisitante` INT NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(50) NOT NULL,
  `data_hora_chegada` DATETIME NOT NULL,
  `tipo_visita_idtipo_visita` INT NOT NULL,
  `moradia_idMoradia` INT NOT NULL,
  `id_usuario_porteiro` INT NOT NULL,
  PRIMARY KEY (`idVisitante`),
  INDEX `fk_visitante_tipo_visita1_idx` (`tipo_visita_idtipo_visita` ASC) VISIBLE,
  INDEX `fk_visitante_moradia1_idx` (`moradia_idMoradia` ASC) VISIBLE,
  INDEX `fk_visitante_usuario1_idx` (`id_usuario_porteiro` ASC) VISIBLE,
  CONSTRAINT `fk_visitante_tipo_visita1`
    FOREIGN KEY (`tipo_visita_idtipo_visita`)
    REFERENCES `domus_db`.`tipo_visita` (`idtTipo_visita`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_visitante_moradia1`
    FOREIGN KEY (`moradia_idMoradia`)
    REFERENCES `domus_db`.`moradia` (`idMoradia`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_visitante_usuario1`
    FOREIGN KEY (`id_usuario_porteiro`)
    REFERENCES `domus_db`.`usuario` (`idUsuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `domus_db`.`aviso_condominial`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`aviso_condominial` (
  `idaviso_condominial` INT NOT NULL AUTO_INCREMENT,
  `titulo` VARCHAR(45) NOT NULL,
  `descricao` VARCHAR(255) NOT NULL,
  `imagem` VARCHAR(255) NULL,
  `data_aviso` DATE NOT NULL,
  `prioridade` TINYINT(1) NOT NULL,
  `tipo_aviso` ENUM('MANUTENCAO', 'EVENTO', 'AVISO', 'NOVIDADE') NOT NULL,
  `data_validade` DATE NOT NULL,
  `id_usuario_sindico` INT NOT NULL,
  `condominio_idCondominio` INT NOT NULL,
  PRIMARY KEY (`idaviso_condominial`),
  INDEX `fk_aviso_condominial_usuario1_idx` (`id_usuario_sindico` ASC) VISIBLE,
  INDEX `fk_aviso_condominial_condominio1_idx` (`condominio_idCondominio` ASC) VISIBLE,
  CONSTRAINT `fk_aviso_condominial_usuario1`
    FOREIGN KEY (`id_usuario_sindico`)
    REFERENCES `domus_db`.`usuario` (`idUsuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_aviso_condominial_condominio1`
    FOREIGN KEY (`condominio_idCondominio`)
    REFERENCES `domus_db`.`condominio` (`idCondominio`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `domus_db`.`espaco_condominial`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`espaco_condominial` (
  `idEspaco_condominial` INT NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(45) NOT NULL,
  `descricao` VARCHAR(255) NOT NULL,
  `capacidade_maxima` VARCHAR(20) NOT NULL,
  `valor_reserva` DECIMAL(10,2) NOT NULL,
  `imagem` VARCHAR(255) NULL,
  `ativo` TINYINT(1) NOT NULL,
  `restricao` VARCHAR(255) NOT NULL,
  `condominio_idCondominio` INT NOT NULL,
  `id_usuario_sindico` INT NOT NULL,
  PRIMARY KEY (`idEspaco_condominial`),
  INDEX `fk_espaco_condominial_condominio1_idx` (`condominio_idCondominio` ASC) VISIBLE,
  INDEX `fk_espaco_condominial_usuario1_idx` (`id_usuario_sindico` ASC) VISIBLE,
  CONSTRAINT `fk_espaco_condominial_condominio1`
    FOREIGN KEY (`condominio_idCondominio`)
    REFERENCES `domus_db`.`condominio` (`idCondominio`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_espaco_condominial_usuario1`
    FOREIGN KEY (`id_usuario_sindico`)
    REFERENCES `domus_db`.`usuario` (`idUsuario`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `domus_db`.`reserva_morador_espaco`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `domus_db`.`reserva_morador_espaco` (
  `idReserva` INT NOT NULL AUTO_INCREMENT,
  `data_reserva` DATE NOT NULL,
  `quantidade_convidados` VARCHAR(45) NOT NULL,
  `status_reserva` ENUM('CONFIRMADA', 'RESERVADA', 'CANCELADA', 'CONCLUIDA') NOT NULL,
  `justificativa_reserva_negada` VARCHAR(255) NULL,
  `espaco_condominial_idEspaco_condominial` INT NOT NULL,
  `morador_idMorador` INT NOT NULL,
  PRIMARY KEY (`idReserva`),
  INDEX `fk_reserva_morador_espaco_espaco_condominial1_idx` (`espaco_condominial_idEspaco_condominial` ASC) VISIBLE,
  INDEX `fk_reserva_morador_espaco_morador1_idx` (`morador_idMorador` ASC) VISIBLE,
  CONSTRAINT `fk_reserva_morador_espaco_espaco_condominial1`
    FOREIGN KEY (`espaco_condominial_idEspaco_condominial`)
    REFERENCES `domus_db`.`espaco_condominial` (`idEspaco_condominial`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_reserva_morador_espaco_morador1`
    FOREIGN KEY (`morador_idMorador`)
    REFERENCES `domus_db`.`morador` (`idMorador`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;