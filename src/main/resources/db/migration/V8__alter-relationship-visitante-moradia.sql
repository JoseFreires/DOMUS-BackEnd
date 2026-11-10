CREATE TABLE visitante_moradia (
    visitante_idVisitante INT NOT NULL,
    moradia_idMoradia INT NOT NULL,

    PRIMARY KEY (
        visitante_idVisitante,
        moradia_idMoradia
    ),

    CONSTRAINT fk_visitante_moradia_visitante
        FOREIGN KEY (visitante_idVisitante)
        REFERENCES visitante (idVisitante)
        ON DELETE CASCADE,

    CONSTRAINT fk_visitante_moradia_moradia
        FOREIGN KEY (moradia_idMoradia)
        REFERENCES moradia (idMoradia)
        ON DELETE CASCADE
);

ALTER TABLE visitante
    DROP FOREIGN KEY fk_visitante_moradia1;

ALTER TABLE visitante
    DROP COLUMN moradia_idMoradia;