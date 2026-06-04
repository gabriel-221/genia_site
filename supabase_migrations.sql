-- =============================================================================
--  GENIA – Script de migração Supabase
--  Execute este script no SQL Editor do seu projeto Supabase
--  (Dashboard → SQL Editor → New Query)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Adicionar campo de opt-in no sistema de GeneMatch
-- -----------------------------------------------------------------------------
ALTER TABLE public.genia_animal
  ADD COLUMN IF NOT EXISTS disponivel_match boolean NOT NULL DEFAULT true;

-- -----------------------------------------------------------------------------
-- 1b. Produção de leite diária (campo adicionado pelo app Android v1.1)
-- -----------------------------------------------------------------------------
ALTER TABLE public.genia_animal
  ADD COLUMN IF NOT EXISTS producao_leite_diaria numeric NOT NULL DEFAULT 0;

-- -----------------------------------------------------------------------------
-- 2. Garantir que user_id existe e está indexado em genia_produtor
--    (a coluna já existe no schema original, mas o índice melhora performance)
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_genia_produtor_user_id
  ON public.genia_produtor(user_id);

CREATE INDEX IF NOT EXISTS idx_genia_animal_produtor_id
  ON public.genia_animal(produtor_id);

CREATE INDEX IF NOT EXISTS idx_genia_animal_disponivel_match
  ON public.genia_animal(disponivel_match);

-- -----------------------------------------------------------------------------
-- 3. Habilitar Row Level Security (RLS)
--    ATENÇÃO: após este passo, apenas as policies abaixo permitem acesso.
--    Certifique-se de que o "Email Confirmations" está desabilitado em
--    Authentication → Settings, ou adapte o fluxo de cadastro.
-- -----------------------------------------------------------------------------
ALTER TABLE public.genia_produtor ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_animal   ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_evento_reprodutivo ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_ciclo_cio          ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_pedigree           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_semen              ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_gene_match         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_genetico_alerta    ENABLE ROW LEVEL SECURITY;

-- -----------------------------------------------------------------------------
-- 4. Policies – genia_produtor
--    Cada usuário autenticado só vê e gerencia o próprio perfil de produtor.
-- -----------------------------------------------------------------------------
DROP POLICY IF EXISTS "produtor_select_proprio"  ON public.genia_produtor;
DROP POLICY IF EXISTS "produtor_insert_proprio"  ON public.genia_produtor;
DROP POLICY IF EXISTS "produtor_update_proprio"  ON public.genia_produtor;
DROP POLICY IF EXISTS "produtor_delete_proprio"  ON public.genia_produtor;

CREATE POLICY "produtor_select_proprio" ON public.genia_produtor
  FOR SELECT USING (user_id = auth.uid());

CREATE POLICY "produtor_insert_proprio" ON public.genia_produtor
  FOR INSERT WITH CHECK (user_id = auth.uid());

CREATE POLICY "produtor_update_proprio" ON public.genia_produtor
  FOR UPDATE USING (user_id = auth.uid());

CREATE POLICY "produtor_delete_proprio" ON public.genia_produtor
  FOR DELETE USING (user_id = auth.uid());

-- -----------------------------------------------------------------------------
-- 5. Policies – genia_animal
--    SELECT: dono vê todos os seus animais + qualquer animal com disponivel_match.
--    INSERT/UPDATE/DELETE: apenas o dono gerencia seus próprios animais.
-- -----------------------------------------------------------------------------
DROP POLICY IF EXISTS "animal_select"  ON public.genia_animal;
DROP POLICY IF EXISTS "animal_insert"  ON public.genia_animal;
DROP POLICY IF EXISTS "animal_update"  ON public.genia_animal;
DROP POLICY IF EXISTS "animal_delete"  ON public.genia_animal;

-- Permite ver os próprios animais (independente de disponivel_match)
-- ou animais de outros produtores marcados como disponíveis para o match.
CREATE POLICY "animal_select" ON public.genia_animal
  FOR SELECT USING (
    disponivel_match = true
    OR
    produtor_id IN (
      SELECT id FROM public.genia_produtor WHERE user_id = auth.uid()
    )
  );

CREATE POLICY "animal_insert" ON public.genia_animal
  FOR INSERT WITH CHECK (
    produtor_id IN (
      SELECT id FROM public.genia_produtor WHERE user_id = auth.uid()
    )
  );

CREATE POLICY "animal_update" ON public.genia_animal
  FOR UPDATE USING (
    produtor_id IN (
      SELECT id FROM public.genia_produtor WHERE user_id = auth.uid()
    )
  );

CREATE POLICY "animal_delete" ON public.genia_animal
  FOR DELETE USING (
    produtor_id IN (
      SELECT id FROM public.genia_produtor WHERE user_id = auth.uid()
    )
  );

-- -----------------------------------------------------------------------------
-- 6. Policies – genia_evento_reprodutivo
--    Acesso vinculado ao dono do animal.
-- -----------------------------------------------------------------------------
DROP POLICY IF EXISTS "evento_acesso_dono" ON public.genia_evento_reprodutivo;

CREATE POLICY "evento_acesso_dono" ON public.genia_evento_reprodutivo
  FOR ALL USING (
    animal_id IN (
      SELECT id FROM public.genia_animal
      WHERE produtor_id IN (
        SELECT id FROM public.genia_produtor WHERE user_id = auth.uid()
      )
    )
  );

-- -----------------------------------------------------------------------------
-- 7. Policies – genia_ciclo_cio
-- -----------------------------------------------------------------------------
DROP POLICY IF EXISTS "ciclo_acesso_dono" ON public.genia_ciclo_cio;

CREATE POLICY "ciclo_acesso_dono" ON public.genia_ciclo_cio
  FOR ALL USING (
    animal_id IN (
      SELECT id FROM public.genia_animal
      WHERE produtor_id IN (
        SELECT id FROM public.genia_produtor WHERE user_id = auth.uid()
      )
    )
  );

-- -----------------------------------------------------------------------------
-- 8. Policies – genia_pedigree
-- -----------------------------------------------------------------------------
DROP POLICY IF EXISTS "pedigree_acesso_dono" ON public.genia_pedigree;

CREATE POLICY "pedigree_acesso_dono" ON public.genia_pedigree
  FOR ALL USING (
    animal_id IN (
      SELECT id FROM public.genia_animal
      WHERE produtor_id IN (
        SELECT id FROM public.genia_produtor WHERE user_id = auth.uid()
      )
    )
  );

-- -----------------------------------------------------------------------------
-- 9. Policies – genia_semen
-- -----------------------------------------------------------------------------
DROP POLICY IF EXISTS "semen_acesso_dono" ON public.genia_semen;

CREATE POLICY "semen_acesso_dono" ON public.genia_semen
  FOR ALL USING (
    produtor_id IN (
      SELECT id FROM public.genia_produtor WHERE user_id = auth.uid()
    )
  );

-- -----------------------------------------------------------------------------
-- 10. Policies – genia_gene_match
--     O solicitante vê seus próprios matches; também permite ver matches
--     envolvendo animais do próprio produtor.
-- -----------------------------------------------------------------------------
DROP POLICY IF EXISTS "genematch_acesso_dono" ON public.genia_gene_match;

CREATE POLICY "genematch_acesso_dono" ON public.genia_gene_match
  FOR ALL USING (
    produtor_solicitante_id IN (
      SELECT id FROM public.genia_produtor WHERE user_id = auth.uid()
    )
    OR
    animal_a_id IN (
      SELECT id FROM public.genia_animal
      WHERE produtor_id IN (
        SELECT id FROM public.genia_produtor WHERE user_id = auth.uid()
      )
    )
    OR
    animal_b_id IN (
      SELECT id FROM public.genia_animal
      WHERE produtor_id IN (
        SELECT id FROM public.genia_produtor WHERE user_id = auth.uid()
      )
    )
  );

-- -----------------------------------------------------------------------------
-- 11. Policies – genia_genetico_alerta
-- -----------------------------------------------------------------------------
DROP POLICY IF EXISTS "alerta_acesso_dono" ON public.genia_genetico_alerta;

CREATE POLICY "alerta_acesso_dono" ON public.genia_genetico_alerta
  FOR ALL USING (
    animal_a_id IN (
      SELECT id FROM public.genia_animal
      WHERE produtor_id IN (
        SELECT id FROM public.genia_produtor WHERE user_id = auth.uid()
      )
    )
  );

-- -----------------------------------------------------------------------------
-- 12. Desabilitar confirmação de e-mail (recomendado para o ambiente de testes)
--     Rode via SQL apenas se tiver permissão; caso contrário, desabilite pelo
--     Dashboard: Authentication → Settings → Disable email confirmations
-- -----------------------------------------------------------------------------
-- UPDATE auth.config SET enable_email_autoconfirm = true;   -- apenas se necessário

-- =============================================================================
--  FIM DO SCRIPT
-- =============================================================================
