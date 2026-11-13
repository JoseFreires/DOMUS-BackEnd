USE domus_db;

CREATE TABLE IF NOT EXISTS morador_moradia (
  morador_idMorador INT NOT NULL,
  moradia_idMoradia INT NOT NULL,
  PRIMARY KEY (morador_idMorador, moradia_idMoradia),
  INDEX fk_morador_moradia_moradia_idx (moradia_idMoradia ASC) VISIBLE,
  CONSTRAINT fk_morador_moradia_morador
    FOREIGN KEY (morador_idMorador)
    REFERENCES morador (idMorador)
    ON DELETE CASCADE,
  CONSTRAINT fk_morador_moradia_moradia
    FOREIGN KEY (moradia_idMoradia)
    REFERENCES moradia (idMoradia)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

-- Backfill com o vínculo atual de morador -> moradia
INSERT INTO morador_moradia (morador_idMorador, moradia_idMoradia)
SELECT idMorador, moradia_idMoradia
FROM morador
WHERE moradia_idMoradia IS NOT NULL;