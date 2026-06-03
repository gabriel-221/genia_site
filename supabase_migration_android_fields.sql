-- Migração: adicionar campos necessários para compatibilidade com o app Android
-- Rodar no Supabase Dashboard → SQL Editor

ALTER TABLE public.genia_animal
  ADD COLUMN IF NOT EXISTS fazenda           text        NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS nome_pai          text        NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS raca_pai          text        NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS rfid_pai          text        NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS nome_mae          text        NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS raca_mae          text        NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS rfid_mae          text        NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS numero_partos     integer     NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS abortos           integer     NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS dias_ultimo_parto integer     NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS filhos_matriz     integer     NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS qualidade_semen   numeric     NOT NULL DEFAULT 3.0,
  ADD COLUMN IF NOT EXISTS filhos_macho      integer     NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS chance_prenhez    numeric,
  ADD COLUMN IF NOT EXISTS prenhou           boolean     NOT NULL DEFAULT false;

-- Campo para guardar o nome do reprodutor nos eventos (o app usa texto livre)
ALTER TABLE public.genia_evento_reprodutivo
  ADD COLUMN IF NOT EXISTS semen_reprodutor  text        NOT NULL DEFAULT '';

-- Política de acesso aberto para o app Android (desabilitar RLS temporariamente
-- OU criar policies permissivas — escolha conforme a necessidade de segurança)
-- OPÇÃO A: desabilitar RLS (apenas para desenvolvimento/hackathon)
ALTER TABLE public.genia_produtor           DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_animal             DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_evento_reprodutivo DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_ciclo_cio          DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_semen              DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_gene_match         DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_genetico_alerta    DISABLE ROW LEVEL SECURITY;

-- OPÇÃO B (produção): criar policies com anon permitido
-- CREATE POLICY "anon_all" ON public.genia_animal FOR ALL USING (true) WITH CHECK (true);
