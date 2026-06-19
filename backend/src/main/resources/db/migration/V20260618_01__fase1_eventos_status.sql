ALTER TABLE eventos
    ADD COLUMN IF NOT EXISTS status_evento VARCHAR(30) NOT NULL DEFAULT 'agendado',
    ADD COLUMN IF NOT EXISTS status_inscricao VARCHAR(30) NOT NULL DEFAULT 'nao_aberta',
    ADD COLUMN IF NOT EXISTS data_inicio TIMESTAMP,
    ADD COLUMN IF NOT EXISTS data_fim TIMESTAMP,
    ADD COLUMN IF NOT EXISTS data_inicio_inscricoes TIMESTAMP,
    ADD COLUMN IF NOT EXISTS capacidade_participantes INT,
    ADD COLUMN IF NOT EXISTS imagem_url VARCHAR(255),
    ADD COLUMN IF NOT EXISTS data_cancelamento TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS motivo_cancelamento TEXT NULL;

UPDATE eventos
SET data_inicio = COALESCE(data_inicio, data_hora, CURRENT_TIMESTAMP + INTERVAL '1 day');

UPDATE eventos
SET data_fim = COALESCE(data_fim, data_inicio + INTERVAL '2 hours');

UPDATE eventos
SET data_inicio_inscricoes = COALESCE(data_inicio_inscricoes, CURRENT_TIMESTAMP);

UPDATE eventos
SET capacidade_participantes = COALESCE(capacidade_participantes, 100);

ALTER TABLE eventos
    ALTER COLUMN data_inicio SET NOT NULL,
    ALTER COLUMN data_fim SET NOT NULL,
    ALTER COLUMN data_inicio_inscricoes SET NOT NULL,
    ALTER COLUMN capacidade_participantes SET NOT NULL;

ALTER TABLE insumos_evento
    ADD COLUMN IF NOT EXISTS responsavel_id BIGINT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_insumos_evento_responsavel'
    ) THEN
        ALTER TABLE insumos_evento
            ADD CONSTRAINT fk_insumos_evento_responsavel
            FOREIGN KEY (responsavel_id) REFERENCES usuarios(id);
    END IF;
END $$;

UPDATE usuarios_eventos ue
SET tipo_relacao = 3
FROM eventos e
WHERE ue.evento_id = e.id
  AND ue.usuario_id = e.organizador_id
  AND ue.tipo_relacao <> 3;

UPDATE usuarios_eventos ue
SET tipo_relacao = 1
WHERE ue.tipo_relacao = 3
  AND NOT EXISTS (
      SELECT 1
      FROM eventos e
      WHERE e.id = ue.evento_id
        AND e.organizador_id = ue.usuario_id
  );

DO $$
DECLARE
    constraint_row RECORD;
BEGIN
    FOR constraint_row IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'usuarios_eventos'::regclass
          AND contype = 'c'
          AND pg_get_constraintdef(oid) ILIKE '%tipo_relacao%'
    LOOP
        EXECUTE format('ALTER TABLE usuarios_eventos DROP CONSTRAINT %I', constraint_row.conname);
    END LOOP;
END $$;

ALTER TABLE usuarios_eventos
    ADD CONSTRAINT usuarios_eventos_tipo_relacao_check
    CHECK (tipo_relacao IN (0, 1, 2, 3));
