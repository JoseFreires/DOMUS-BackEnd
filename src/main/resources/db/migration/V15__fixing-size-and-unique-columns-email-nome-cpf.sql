-- Padronizar e-mail como UNIQUE
ALTER TABLE conta_adm
ADD CONSTRAINT uk_conta_adm_email UNIQUE (email);


-- Padronizar CPF como UNIQUE
ALTER TABLE convidado
ADD CONSTRAINT uk_convidado_cpf UNIQUE (cpf);

ALTER TABLE pessoa_autorizada
ADD CONSTRAINT uk_pessoa_autorizada_cpf UNIQUE (cpf);


-- Padronizar senha como VARCHAR(255)
ALTER TABLE conta_adm
MODIFY COLUMN senha VARCHAR(255) NOT NULL;


-- Padronizar telefone como VARCHAR(14)
ALTER TABLE convidado
MODIFY COLUMN telefone VARCHAR(14) NOT NULL;


-- Padronizar nomes de pessoas como VARCHAR(200)
ALTER TABLE convidado
MODIFY COLUMN nome_convidado VARCHAR(200) NOT NULL;

ALTER TABLE pessoa_autorizada
MODIFY COLUMN nome VARCHAR(200) NOT NULL;

ALTER TABLE visitante
MODIFY COLUMN nome VARCHAR(200) NOT NULL;