DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM usuarios
        WHERE cpf IS NOT NULL
        GROUP BY regexp_replace(cpf, '\D', '', 'g')
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Existem CPFs duplicados após normalização. Corrija os dados antes de aplicar a migration.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM usuarios
        WHERE telefone IS NOT NULL
        GROUP BY regexp_replace(telefone, '\D', '', 'g')
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Existem telefones duplicados após normalização. Corrija os dados antes de aplicar a migration.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM usuarios
        WHERE email IS NOT NULL
        GROUP BY lower(trim(email))
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Existem emails duplicados sem considerar maiúsculas/minúsculas. Corrija os dados antes de aplicar a migration.';
    END IF;
END $$;

UPDATE usuarios
SET cpf = regexp_replace(cpf, '\D', '', 'g')
WHERE cpf IS NOT NULL;

UPDATE usuarios
SET telefone = regexp_replace(telefone, '\D', '', 'g')
WHERE telefone IS NOT NULL;

UPDATE usuarios
SET email = lower(trim(email))
WHERE email IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_usuarios_email' AND conrelid = 'usuarios'::regclass
    ) THEN
        ALTER TABLE usuarios ADD CONSTRAINT uk_usuarios_email UNIQUE (email);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_usuarios_cpf' AND conrelid = 'usuarios'::regclass
    ) THEN
        ALTER TABLE usuarios ADD CONSTRAINT uk_usuarios_cpf UNIQUE (cpf);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_usuarios_telefone' AND conrelid = 'usuarios'::regclass
    ) THEN
        ALTER TABLE usuarios ADD CONSTRAINT uk_usuarios_telefone UNIQUE (telefone);
    END IF;
END $$;
