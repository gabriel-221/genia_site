-- =============================================================================
--  Adiciona ovinos e caprinos ao produtor1@teste.com
--  produtor_id = '11111111-0001-0001-0001-000000000001'
--  Execute no Supabase Dashboard → SQL Editor → New Query
-- =============================================================================

ALTER TABLE public.genia_animal DISABLE ROW LEVEL SECURITY;

INSERT INTO public.genia_animal
  (id, produtor_id, nome, especie, raca, sexo, data_nascimento,
   peso_kg, escore_corporal, fazenda,
   numero_partos, abortos, dias_ultimo_parto, filhos_matriz,
   qualidade_semen, filhos_macho, producao_leite_diaria, prenhou, disponivel_match)
VALUES

-- ── OVINOS ────────────────────────────────────────────────────────────────────
  ('a1000008-0000-0000-0000-000000000001', '11111111-0001-0001-0001-000000000001',
   'Ramses', 'ovino', 'Dorper', 'macho', '2022-03-10',
   88, 4.2, 'Fazenda Boa Esperança',
   0, 0, 0, 0, 4.5, 12, 0, false, true),

  ('a1000009-0000-0000-0000-000000000001', '11111111-0001-0001-0001-000000000001',
   'Sereia', 'ovino', 'Santa Inês', 'femea', '2022-07-15',
   52, 3.8, 'Fazenda Boa Esperança',
   2, 0, 45, 3, 0, 0, 0, true, true),

  ('a1000010-0000-0000-0000-000000000001', '11111111-0001-0001-0001-000000000001',
   'Tempestade', 'ovino', 'Texel', 'macho', '2021-11-20',
   90, 4.3, 'Fazenda Boa Esperança',
   0, 0, 0, 0, 4.7, 18, 0, false, true),

  ('a1000011-0000-0000-0000-000000000001', '11111111-0001-0001-0001-000000000001',
   'Boneca', 'ovino', 'Morada Nova', 'femea', '2023-02-08',
   44, 3.4, 'Fazenda Boa Esperança',
   0, 0, 0, 0, 0, 0, 0, false, true),

-- ── CAPRINOS ──────────────────────────────────────────────────────────────────
  ('a1000012-0000-0000-0000-000000000001', '11111111-0001-0001-0001-000000000001',
   'Guerreiro', 'caprino', 'Boer', 'macho', '2021-08-05',
   82, 4.4, 'Fazenda Boa Esperança',
   0, 0, 0, 0, 4.6, 22, 0, false, true),

  ('a1000013-0000-0000-0000-000000000001', '11111111-0001-0001-0001-000000000001',
   'Dalila', 'caprino', 'Anglo-Nubiana', 'femea', '2022-01-12',
   50, 3.9, 'Fazenda Boa Esperança',
   2, 0, 55, 3, 0, 0, 2.8, true, true),

  ('a1000014-0000-0000-0000-000000000001', '11111111-0001-0001-0001-000000000001',
   'Sabina', 'caprino', 'Saanen', 'femea', '2021-05-20',
   55, 4.0, 'Fazenda Boa Esperança',
   3, 0, 40, 4, 0, 0, 3.5, false, true),

  ('a1000015-0000-0000-0000-000000000001', '11111111-0001-0001-0001-000000000001',
   'Faraó', 'caprino', 'Boer', 'macho', '2022-09-30',
   78, 4.1, 'Fazenda Boa Esperança',
   0, 0, 0, 0, 4.3, 8, 0, false, false);

ALTER TABLE public.genia_animal ENABLE ROW LEVEL SECURITY;

-- Resultado: produtor1 terá 15 animais (7 bovinos + 4 ovinos + 4 caprinos)
