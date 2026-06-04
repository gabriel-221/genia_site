-- ============================================================
-- G.E.N.I.A — Seed de dados para teste do site público NFC
-- Execute no SQL Editor do Supabase (como service role)
-- ============================================================
--
-- Animais criados e suas URLs de teste:
--
--  Animal          Espécie  Sexo   Tag NFC        URL
--  Mimosa          Bovino   Fêmea  NFC-BOV-003    /a/NFC-BOV-003  ← showcase completo
--  Trovão          Bovino   Macho  NFC-BOV-001    /a/NFC-BOV-001
--  Beleza          Bovino   Fêmea  NFC-BOV-002    /a/NFC-BOV-002
--  Estrela         Bovino   Fêmea  NFC-BOV-004    /a/NFC-BOV-004
--  Sultan          Ovino    Macho  NFC-OVI-001    /a/NFC-OVI-001  ← design azul
--  Flor            Caprino  Fêmea  NFC-CAP-001    /a/NFC-CAP-001  ← design verde
-- ============================================================

-- ============================================================
-- PARTE 1 — Policies públicas para o site NFC
-- (sem essas policies o site retorna dados null no join)
-- ============================================================

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE tablename = 'genia_produtor' AND policyname = 'produtor_publico_leitura'
  ) THEN
    EXECUTE 'CREATE POLICY "produtor_publico_leitura" ON genia_produtor
             FOR SELECT USING (true)';
    RAISE NOTICE 'Policy produtor_publico_leitura criada.';
  ELSE
    RAISE NOTICE 'Policy produtor_publico_leitura já existe.';
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE tablename = 'genia_pedigree' AND policyname = 'pedigree_publico_leitura'
  ) THEN
    EXECUTE 'CREATE POLICY "pedigree_publico_leitura" ON genia_pedigree
             FOR SELECT USING (true)';
    RAISE NOTICE 'Policy pedigree_publico_leitura criada.';
  ELSE
    RAISE NOTICE 'Policy pedigree_publico_leitura já existe.';
  END IF;
END $$;

DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies
    WHERE tablename = 'genia_evento_reprodutivo' AND policyname = 'evento_publico_leitura'
  ) THEN
    EXECUTE 'CREATE POLICY "evento_publico_leitura" ON genia_evento_reprodutivo
             FOR SELECT USING (true)';
    RAISE NOTICE 'Policy evento_publico_leitura criada.';
  ELSE
    RAISE NOTICE 'Policy evento_publico_leitura já existe.';
  END IF;
END $$;

-- ============================================================
-- PARTE 2 — Produtor de teste
-- ============================================================

INSERT INTO genia_produtor (
  id,
  user_id,
  nome,
  telefone,
  municipio,
  estado,
  nome_fazenda,
  hectares,
  ativo
) VALUES (
  'a0000000-0000-0000-0000-000000000001',
  NULL,
  'João Rodrigues da Silva',
  '(88) 99876-5432',
  'Crateús',
  'CE',
  'Fazenda Serra Verde',
  380,
  true
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- PARTE 3 — Animais
-- ============================================================

-- PAI: Trovão (Bovino Nelore, Macho) — gradiente âmbar
INSERT INTO genia_animal (
  id, produtor_id,
  nome, especie, raca, linhagem, sexo,
  data_nascimento, rfid_tag, slug_publico,
  peso_kg, escore_corporal, coef_endogamia,
  ativo
) VALUES (
  'a1000000-0000-0000-0000-000000000001',
  'a0000000-0000-0000-0000-000000000001',
  'Trovão', 'bovino', 'Nelore', 'Linhagem Prata',
  'macho', '2018-03-10', 'NFC-BOV-001', 'trovao-nelore-001',
  648, 4.5, 0.0320,
  true
)
ON CONFLICT (id) DO NOTHING;

-- MÃE: Beleza (Bovino Nelore, Fêmea)
INSERT INTO genia_animal (
  id, produtor_id,
  nome, especie, raca, linhagem, sexo,
  data_nascimento, rfid_tag, slug_publico,
  peso_kg, escore_corporal, coef_endogamia,
  ativo
) VALUES (
  'a1000000-0000-0000-0000-000000000002',
  'a0000000-0000-0000-0000-000000000001',
  'Beleza', 'bovino', 'Nelore', 'Linhagem Ouro',
  'femea', '2019-07-22', 'NFC-BOV-002', 'beleza-nelore-002',
  432, 3.8, 0.0180,
  true
)
ON CONFLICT (id) DO NOTHING;

-- FILHA PRINCIPAL: Mimosa (Bovino Nelore, Fêmea) ← animal showcase do site
INSERT INTO genia_animal (
  id, produtor_id,
  nome, especie, raca, linhagem, sexo,
  data_nascimento, rfid_tag, slug_publico,
  peso_kg, escore_corporal, coef_endogamia,
  ativo
) VALUES (
  'a2000000-0000-0000-0000-000000000001',
  'a0000000-0000-0000-0000-000000000001',
  'Mimosa', 'bovino', 'Nelore', 'Ouro × Prata',
  'femea', '2021-04-15', 'NFC-BOV-003', 'mimosa-nelore-003',
  387, 4.2, 0.0210,
  true
)
ON CONFLICT (id) DO NOTHING;

-- FILHA de Mimosa: Estrela
INSERT INTO genia_animal (
  id, produtor_id,
  nome, especie, raca, linhagem, sexo,
  data_nascimento, rfid_tag, slug_publico,
  peso_kg, escore_corporal, coef_endogamia,
  ativo
) VALUES (
  'a3000000-0000-0000-0000-000000000001',
  'a0000000-0000-0000-0000-000000000001',
  'Estrela', 'bovino', 'Nelore', 'Ouro × Prata',
  'femea', '2023-09-05', 'NFC-BOV-004', 'estrela-nelore-004',
  214, 3.5, 0.0380,
  true
)
ON CONFLICT (id) DO NOTHING;

-- OVINO: Sultan (Santa Inês, Macho) — testa gradiente azul + sem pedigree/reprodução
INSERT INTO genia_animal (
  id, produtor_id,
  nome, especie, raca, linhagem, sexo,
  data_nascimento, rfid_tag, slug_publico,
  peso_kg, escore_corporal, coef_endogamia,
  ativo
) VALUES (
  'a4000000-0000-0000-0000-000000000001',
  'a0000000-0000-0000-0000-000000000001',
  'Sultan', 'ovino', 'Santa Inês', 'Sertão Livre',
  'macho', '2022-01-18', 'NFC-OVI-001', 'sultan-santa-ines-001',
  79, 4.0, 0.0150,
  true
)
ON CONFLICT (id) DO NOTHING;

-- CAPRINO: Flor (Moxotó, Fêmea) — testa gradiente verde + card de reprodução
INSERT INTO genia_animal (
  id, produtor_id,
  nome, especie, raca, linhagem, sexo,
  data_nascimento, rfid_tag, slug_publico,
  peso_kg, escore_corporal, coef_endogamia,
  ativo
) VALUES (
  'a5000000-0000-0000-0000-000000000001',
  'a0000000-0000-0000-0000-000000000001',
  'Flor', 'caprino', 'Moxotó', 'Inhamuns',
  'femea', '2021-11-03', 'NFC-CAP-001', 'flor-moxoto-001',
  43, 3.7, 0.0090,
  true
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- PARTE 4 — Pedigree (genealogia)
-- ============================================================

-- Mimosa: pai = Trovão, mãe = Beleza
INSERT INTO genia_pedigree (id, animal_id, pai_id, mae_id, geracao)
VALUES (
  'b1000000-0000-0000-0000-000000000001',
  'a2000000-0000-0000-0000-000000000001',  -- Mimosa
  'a1000000-0000-0000-0000-000000000001',  -- Trovão
  'a1000000-0000-0000-0000-000000000002',  -- Beleza
  1
)
ON CONFLICT (id) DO NOTHING;

-- Estrela: pai = Trovão, mãe = Mimosa
INSERT INTO genia_pedigree (id, animal_id, pai_id, mae_id, geracao)
VALUES (
  'b1000000-0000-0000-0000-000000000002',
  'a3000000-0000-0000-0000-000000000001',  -- Estrela
  'a1000000-0000-0000-0000-000000000001',  -- Trovão
  'a2000000-0000-0000-0000-000000000001',  -- Mimosa
  2
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- PARTE 5 — Eventos reprodutivos de Mimosa (bovino fêmea)
-- 3 inseminações: 1 falha + 1 sucesso (gerou Estrela) + 1 recente em andamento
-- Taxa de prenhez calculada pelo site: 2/3 = 67%
-- score_ia_prenhez do último evento sobrescreve: 78%
-- ============================================================

-- Inseminação 1 (2022): não confirmou gestação
INSERT INTO genia_evento_reprodutivo (
  id, animal_id, tipo, data_evento,
  tecnico_responsavel, gestacao_confirmada, score_ia_prenhez,
  fatores_ia, observacoes
) VALUES (
  'c1000000-0000-0000-0000-000000000001',
  'a2000000-0000-0000-0000-000000000001',
  'inseminacao_artificial', '2022-06-10',
  'Dr. Antônio Sousa', false, 0.4200,
  '{"historico_reprodutivo":0.6,"escore_corporal":0.7,"qualidade_semen":0.5,"epoca_ano":0.4}',
  'Estro fraco. Repetição de cio confirmada.'
)
ON CONFLICT (id) DO NOTHING;

-- Inseminação 2 (2023): gestação confirmada → gerou Estrela
INSERT INTO genia_evento_reprodutivo (
  id, animal_id, tipo, data_evento,
  tecnico_responsavel, gestacao_confirmada, data_parto_previsto, score_ia_prenhez,
  fatores_ia
) VALUES (
  'c1000000-0000-0000-0000-000000000002',
  'a2000000-0000-0000-0000-000000000001',
  'inseminacao_artificial', '2023-01-15',
  'Dr. Antônio Sousa', true, '2023-10-22', 0.8100,
  '{"historico_reprodutivo":0.8,"escore_corporal":0.9,"qualidade_semen":0.8,"epoca_ano":0.7}'
)
ON CONFLICT (id) DO NOTHING;

-- Diagnóstico de gestação
INSERT INTO genia_evento_reprodutivo (
  id, animal_id, tipo, data_evento,
  tecnico_responsavel, gestacao_confirmada
) VALUES (
  'c1000000-0000-0000-0000-000000000003',
  'a2000000-0000-0000-0000-000000000001',
  'diagnostico_gestacao', '2023-03-20',
  'Dr. Antônio Sousa', true
)
ON CONFLICT (id) DO NOTHING;

-- Parto (nasceu Estrela)
INSERT INTO genia_evento_reprodutivo (
  id, animal_id, tipo, data_evento,
  tecnico_responsavel, observacoes
) VALUES (
  'c1000000-0000-0000-0000-000000000004',
  'a2000000-0000-0000-0000-000000000001',
  'parto', '2023-09-05',
  'Dr. Antônio Sousa',
  'Parto eutócico. Bezerra saudável (Estrela). Peso ao nascer: 28 kg.'
)
ON CONFLICT (id) DO NOTHING;

-- Inseminação 3 (2025): mais recente — este é o "último evento" exibido no site
INSERT INTO genia_evento_reprodutivo (
  id, animal_id, tipo, data_evento,
  tecnico_responsavel, gestacao_confirmada, data_parto_previsto, score_ia_prenhez,
  fatores_ia
) VALUES (
  'c1000000-0000-0000-0000-000000000005',
  'a2000000-0000-0000-0000-000000000001',
  'inseminacao_artificial', '2025-03-10',
  'Dr. Antônio Sousa', true, '2025-12-15', 0.7800,
  '{"historico_reprodutivo":0.9,"escore_corporal":0.85,"qualidade_semen":0.75,"epoca_ano":0.6}'
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- PARTE 6 — Eventos reprodutivos de Flor (caprino fêmea)
-- ============================================================

-- Inseminação 1: sucesso
INSERT INTO genia_evento_reprodutivo (
  id, animal_id, tipo, data_evento,
  tecnico_responsavel, gestacao_confirmada, data_parto_previsto, score_ia_prenhez
) VALUES (
  'c2000000-0000-0000-0000-000000000001',
  'a5000000-0000-0000-0000-000000000001',
  'inseminacao_artificial', '2023-08-05',
  'Dr. Paulo Melo', true, '2024-01-10', 0.8800
)
ON CONFLICT (id) DO NOTHING;

-- Parto gemelar
INSERT INTO genia_evento_reprodutivo (
  id, animal_id, tipo, data_evento,
  tecnico_responsavel, observacoes
) VALUES (
  'c2000000-0000-0000-0000-000000000002',
  'a5000000-0000-0000-0000-000000000001',
  'parto', '2024-01-08',
  'Dr. Paulo Melo',
  'Parto gemelar. 2 cabritos, ambos saudáveis. Total: 5,8 kg.'
)
ON CONFLICT (id) DO NOTHING;

-- Inseminação 2: mais recente
INSERT INTO genia_evento_reprodutivo (
  id, animal_id, tipo, data_evento,
  tecnico_responsavel, gestacao_confirmada, data_parto_previsto, score_ia_prenhez
) VALUES (
  'c2000000-0000-0000-0000-000000000003',
  'a5000000-0000-0000-0000-000000000001',
  'inseminacao_artificial', '2025-04-20',
  'Dr. Paulo Melo', true, '2025-09-25', 0.9100
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- VERIFICAÇÃO FINAL
-- ============================================================

SELECT
  a.nome,
  a.especie::text,
  a.sexo::text,
  a.rfid_tag,
  a.escore_corporal,
  a.peso_kg,
  p.nome_fazenda,
  (SELECT COUNT(*) FROM genia_pedigree pg WHERE pg.animal_id = a.id) AS tem_pedigree,
  (SELECT COUNT(*) FROM genia_evento_reprodutivo ev WHERE ev.animal_id = a.id) AS total_eventos
FROM genia_animal a
LEFT JOIN genia_produtor p ON a.produtor_id = p.id
WHERE a.produtor_id = 'a0000000-0000-0000-0000-000000000001'
ORDER BY a.especie, a.sexo, a.nome;
