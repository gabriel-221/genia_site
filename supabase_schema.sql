-- =============================================================================
--  GENIA – Schema completo do banco de dados Supabase
--  Use este arquivo para recriar o banco do zero em um novo projeto Supabase.
--  Execute no Dashboard → SQL Editor → New Query
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 0. Extensões necessárias
-- ---------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ---------------------------------------------------------------------------
-- 1. Enums personalizados
-- ---------------------------------------------------------------------------
DO $$ BEGIN
  CREATE TYPE genia_especie_enum AS ENUM ('bovino', 'ovino', 'caprino');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
  CREATE TYPE genia_sexo_enum AS ENUM ('macho', 'femea');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
  CREATE TYPE genia_evento_tipo_enum AS ENUM ('cio', 'inseminacao', 'diagnostico', 'parto', 'desmame');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
  CREATE TYPE genia_match_status_enum AS ENUM ('pendente', 'aceito', 'recusado', 'concluido');
EXCEPTION WHEN duplicate_object THEN null; END $$;

DO $$ BEGIN
  CREATE TYPE genia_risco_enum AS ENUM ('baixo', 'medio', 'alto');
EXCEPTION WHEN duplicate_object THEN null; END $$;

-- ---------------------------------------------------------------------------
-- 2. Tabela: genia_produtor
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.genia_produtor (
  id              uuid        NOT NULL DEFAULT uuid_generate_v4(),
  user_id         uuid,
  nome            text        NOT NULL,
  cpf             text        UNIQUE,
  telefone        text,
  municipio       text        NOT NULL DEFAULT '',
  estado          text        NOT NULL DEFAULT 'CE',
  nome_fazenda    text,
  hectares        numeric,
  foto_url        text,
  ativo           boolean     NOT NULL DEFAULT true,
  criado_em       timestamptz NOT NULL DEFAULT now(),
  atualizado_em   timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT genia_produtor_pkey PRIMARY KEY (id),
  CONSTRAINT genia_produtor_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);

-- ---------------------------------------------------------------------------
-- 3. Tabela: genia_animal
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.genia_animal (
  id                    uuid        NOT NULL DEFAULT uuid_generate_v4(),
  produtor_id           uuid        NOT NULL,
  nome                  text        NOT NULL,
  especie               text        NOT NULL,   -- 'bovino' | 'ovino' | 'caprino'
  raca                  text        NOT NULL DEFAULT '',
  linhagem              text,
  sexo                  text        NOT NULL,   -- 'macho' | 'femea'
  data_nascimento       date,
  rfid_tag              text        UNIQUE,
  slug_publico          text        DEFAULT (uuid_generate_v4())::text UNIQUE,
  peso_kg               numeric,
  escore_corporal       numeric     CHECK (escore_corporal >= 1 AND escore_corporal <= 5),
  coef_endogamia        numeric     DEFAULT 0,
  foto_url              text,
  ativo                 boolean     NOT NULL DEFAULT true,
  criado_em             timestamptz NOT NULL DEFAULT now(),
  atualizado_em         timestamptz NOT NULL DEFAULT now(),

  -- Campos de localização / pedigree
  fazenda               text        NOT NULL DEFAULT '',
  nome_pai              text        NOT NULL DEFAULT '',
  raca_pai              text        NOT NULL DEFAULT '',
  rfid_pai              text        NOT NULL DEFAULT '',
  nome_mae              text        NOT NULL DEFAULT '',
  raca_mae              text        NOT NULL DEFAULT '',
  rfid_mae              text        NOT NULL DEFAULT '',

  -- Atributos Matriz (Fêmea)
  numero_partos         integer     NOT NULL DEFAULT 0,
  abortos               integer     NOT NULL DEFAULT 0,
  dias_ultimo_parto     integer     NOT NULL DEFAULT 0,
  filhos_matriz         integer     NOT NULL DEFAULT 0,
  producao_leite_diaria numeric     NOT NULL DEFAULT 0,  -- litros/dia

  -- Atributos Reprodutor (Macho)
  qualidade_semen       numeric     NOT NULL DEFAULT 3.0,
  filhos_macho          integer     NOT NULL DEFAULT 0,

  -- Saída de IA
  chance_prenhez        numeric,
  prenhou               boolean     NOT NULL DEFAULT false,

  -- NFC
  nfc_ativo             boolean     NOT NULL DEFAULT false,
  nfc_gravada_em        timestamptz,

  -- GeneMatch
  disponivel_match      boolean     NOT NULL DEFAULT true,

  CONSTRAINT genia_animal_pkey      PRIMARY KEY (id),
  CONSTRAINT genia_animal_produtor  FOREIGN KEY (produtor_id) REFERENCES public.genia_produtor(id)
);

-- ---------------------------------------------------------------------------
-- 4. Tabela: genia_pedigree
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.genia_pedigree (
  id         uuid        NOT NULL DEFAULT uuid_generate_v4(),
  animal_id  uuid        NOT NULL,
  pai_id     uuid,
  mae_id     uuid,
  geracao    integer     NOT NULL DEFAULT 0,
  criado_em  timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT genia_pedigree_pkey      PRIMARY KEY (id),
  CONSTRAINT genia_pedigree_animal    FOREIGN KEY (animal_id) REFERENCES public.genia_animal(id),
  CONSTRAINT genia_pedigree_pai       FOREIGN KEY (pai_id)    REFERENCES public.genia_animal(id),
  CONSTRAINT genia_pedigree_mae       FOREIGN KEY (mae_id)    REFERENCES public.genia_animal(id)
);

-- ---------------------------------------------------------------------------
-- 5. Tabela: genia_semen
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.genia_semen (
  id               uuid        NOT NULL DEFAULT uuid_generate_v4(),
  produtor_id      uuid        NOT NULL,
  nome_reprodutor  text        NOT NULL,
  raca             text        NOT NULL,
  especie          text        NOT NULL,
  registro         text,
  motilidade_pct   numeric,
  concentracao     integer,
  origem           text,
  preco_dose       numeric,
  estoque_doses    integer     DEFAULT 0,
  ativo            boolean     NOT NULL DEFAULT true,
  criado_em        timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT genia_semen_pkey     PRIMARY KEY (id),
  CONSTRAINT genia_semen_produtor FOREIGN KEY (produtor_id) REFERENCES public.genia_produtor(id)
);

-- ---------------------------------------------------------------------------
-- 6. Tabela: genia_ciclo_cio
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.genia_ciclo_cio (
  id               uuid        NOT NULL DEFAULT uuid_generate_v4(),
  animal_id        uuid        NOT NULL,
  data_deteccao    date        NOT NULL,
  proxima_previsao date,
  inseminado       boolean     NOT NULL DEFAULT false,
  observacoes      text,
  criado_em        timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT genia_ciclo_cio_pkey   PRIMARY KEY (id),
  CONSTRAINT genia_ciclo_cio_animal FOREIGN KEY (animal_id) REFERENCES public.genia_animal(id)
);

-- ---------------------------------------------------------------------------
-- 7. Tabela: genia_evento_reprodutivo
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.genia_evento_reprodutivo (
  id                  uuid        NOT NULL DEFAULT uuid_generate_v4(),
  animal_id           uuid        NOT NULL,
  tipo                text        NOT NULL,   -- 'cio' | 'inseminacao' | 'diagnostico' | 'parto' | 'desmame'
  data_evento         date        NOT NULL,
  semen_id            uuid,
  semen_reprodutor    text        NOT NULL DEFAULT '',
  tecnico_responsavel text,
  gestacao_confirmada boolean,
  data_parto_previsto date,
  score_ia_prenhez    numeric,
  fatores_ia          jsonb,
  observacoes         text,
  criado_em           timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT genia_evento_pkey   PRIMARY KEY (id),
  CONSTRAINT genia_evento_animal FOREIGN KEY (animal_id) REFERENCES public.genia_animal(id),
  CONSTRAINT genia_evento_semen  FOREIGN KEY (semen_id)  REFERENCES public.genia_semen(id)
);

-- ---------------------------------------------------------------------------
-- 8. Tabela: genia_gene_match
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.genia_gene_match (
  id                     uuid        NOT NULL DEFAULT uuid_generate_v4(),
  animal_a_id            uuid        NOT NULL,
  animal_b_id            uuid        NOT NULL,
  produtor_solicitante_id uuid       NOT NULL,
  score_compatibilidade  numeric     NOT NULL DEFAULT 0,
  coef_parentesco        numeric     DEFAULT 0,
  dgc_estimado           numeric,
  score_peso_prole       numeric,
  status                 text        NOT NULL DEFAULT 'pendente',
  criado_em              timestamptz NOT NULL DEFAULT now(),
  atualizado_em          timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT genia_match_pkey     PRIMARY KEY (id),
  CONSTRAINT genia_match_animal_a FOREIGN KEY (animal_a_id)             REFERENCES public.genia_animal(id),
  CONSTRAINT genia_match_animal_b FOREIGN KEY (animal_b_id)             REFERENCES public.genia_animal(id),
  CONSTRAINT genia_match_produtor FOREIGN KEY (produtor_solicitante_id) REFERENCES public.genia_produtor(id)
);

-- ---------------------------------------------------------------------------
-- 9. Tabela: genia_genetico_alerta
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.genia_genetico_alerta (
  id               uuid        NOT NULL DEFAULT uuid_generate_v4(),
  animal_a_id      uuid        NOT NULL,
  animal_b_id      uuid        NOT NULL,
  nivel_risco      text        NOT NULL,   -- 'baixo' | 'medio' | 'alto'
  coef_parentesco  numeric,
  descricao        text        NOT NULL,
  resolvido        boolean     NOT NULL DEFAULT false,
  criado_em        timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT genia_alerta_pkey     PRIMARY KEY (id),
  CONSTRAINT genia_alerta_animal_a FOREIGN KEY (animal_a_id) REFERENCES public.genia_animal(id),
  CONSTRAINT genia_alerta_animal_b FOREIGN KEY (animal_b_id) REFERENCES public.genia_animal(id)
);

-- ---------------------------------------------------------------------------
-- 10. Índices de performance
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_produtor_user_id         ON public.genia_produtor(user_id);
CREATE INDEX IF NOT EXISTS idx_animal_produtor_id       ON public.genia_animal(produtor_id);
CREATE INDEX IF NOT EXISTS idx_animal_disponivel_match  ON public.genia_animal(disponivel_match);
CREATE INDEX IF NOT EXISTS idx_evento_animal_id         ON public.genia_evento_reprodutivo(animal_id);
CREATE INDEX IF NOT EXISTS idx_ciclo_animal_id          ON public.genia_ciclo_cio(animal_id);

-- ---------------------------------------------------------------------------
-- 11. Row Level Security (RLS)
-- ---------------------------------------------------------------------------
ALTER TABLE public.genia_produtor            ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_animal              ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_evento_reprodutivo  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_ciclo_cio           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_pedigree            ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_semen               ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_gene_match          ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_genetico_alerta     ENABLE ROW LEVEL SECURITY;

-- Produtor: usuário vê e gerencia apenas o próprio perfil
CREATE POLICY "produtor_proprio" ON public.genia_produtor
  FOR ALL USING (user_id = auth.uid());

-- Animal: dono vê todos os seus; qualquer um vê animais marcados para match
CREATE POLICY "animal_select" ON public.genia_animal
  FOR SELECT USING (
    disponivel_match = true
    OR produtor_id IN (SELECT id FROM public.genia_produtor WHERE user_id = auth.uid())
  );
CREATE POLICY "animal_write" ON public.genia_animal
  FOR ALL USING (
    produtor_id IN (SELECT id FROM public.genia_produtor WHERE user_id = auth.uid())
  );

-- Demais tabelas: acesso pelo dono do animal
CREATE POLICY "evento_dono" ON public.genia_evento_reprodutivo
  FOR ALL USING (
    animal_id IN (
      SELECT id FROM public.genia_animal
      WHERE produtor_id IN (SELECT id FROM public.genia_produtor WHERE user_id = auth.uid())
    )
  );

CREATE POLICY "ciclo_dono" ON public.genia_ciclo_cio
  FOR ALL USING (
    animal_id IN (
      SELECT id FROM public.genia_animal
      WHERE produtor_id IN (SELECT id FROM public.genia_produtor WHERE user_id = auth.uid())
    )
  );

CREATE POLICY "pedigree_dono" ON public.genia_pedigree
  FOR ALL USING (
    animal_id IN (
      SELECT id FROM public.genia_animal
      WHERE produtor_id IN (SELECT id FROM public.genia_produtor WHERE user_id = auth.uid())
    )
  );

CREATE POLICY "semen_dono" ON public.genia_semen
  FOR ALL USING (
    produtor_id IN (SELECT id FROM public.genia_produtor WHERE user_id = auth.uid())
  );

CREATE POLICY "match_dono" ON public.genia_gene_match
  FOR ALL USING (
    produtor_solicitante_id IN (SELECT id FROM public.genia_produtor WHERE user_id = auth.uid())
    OR animal_a_id IN (
      SELECT id FROM public.genia_animal
      WHERE produtor_id IN (SELECT id FROM public.genia_produtor WHERE user_id = auth.uid())
    )
  );

CREATE POLICY "alerta_dono" ON public.genia_genetico_alerta
  FOR ALL USING (
    animal_a_id IN (
      SELECT id FROM public.genia_animal
      WHERE produtor_id IN (SELECT id FROM public.genia_produtor WHERE user_id = auth.uid())
    )
  );

-- =============================================================================
--  FIM DO SCHEMA
-- =============================================================================
