ALTER TABLE espaco_condominial
MODIFY COLUMN capacidade_maxima INT UNSIGNED NOT NULL;

ALTER TABLE reserva_morador_espaco
MODIFY COLUMN quantidade_convidados INT UNSIGNED NOT NULL;