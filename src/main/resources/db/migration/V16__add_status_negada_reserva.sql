ALTER TABLE reserva_morador_espaco
MODIFY COLUMN status_reserva
ENUM(
    'CONFIRMADA',
    'RESERVADA',
    'CANCELADA',
    'CONCLUIDA',
    'NEGADA'
) NOT NULL;