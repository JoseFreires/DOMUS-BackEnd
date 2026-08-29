ALTER TABLE `conta_adm`
    ADD COLUMN `papel_idPapel` INT NOT NULL,
  ADD INDEX `fk_conta_adm_papel_idx` (`papel_idPapel` ASC),
  ADD CONSTRAINT `fk_conta_adm_papel`
    FOREIGN KEY (`papel_idPapel`)
    REFERENCES `papel` (`idPapel`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;