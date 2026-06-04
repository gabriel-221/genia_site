-- =============================================================================
--  GENIA — Script de seed para demonstração no Hackathon Expoagro Crateús 2026
--  Execute no Dashboard → SQL Editor → New Query
--  Popula 7 produtores com animais reais para testes na apresentação
--
--  Senhas configuradas no Supabase Auth: Teste@123 (para todos)
-- =============================================================================

-- Desabilita RLS temporariamente para o seed funcionar sem sessão ativa
ALTER TABLE public.genia_produtor           DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_animal             DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_evento_reprodutivo DISABLE ROW LEVEL SECURITY;

-- Limpa dados anteriores dos produtores de teste (seguro repetir)
DELETE FROM public.genia_evento_reprodutivo
  WHERE animal_id IN (
    SELECT id FROM public.genia_animal
    WHERE produtor_id IN (
      SELECT id FROM public.genia_produtor
      WHERE user_id IN (
        'c2bd0ee3-7119-412c-ada9-11fc636d40e4',
        '8a620403-5bf0-4e74-a00d-b8a78066e3f9',
        'd4b1a9c9-11aa-4edf-bf90-d35e99c3a0a4',
        'e33951d5-0493-476b-bf11-887099a2f1c2',
        '815855ba-c43e-4a83-ba36-3f093391fa3b',
        '73bab68e-c06b-4bdf-821d-8d1eb036e3e4',
        '9a0ebc06-0ef9-4d37-a6fa-9eaebe363206',
        'fbde59e0-f830-4cd9-9c71-3add833fb1eb'
      )
    )
  );

DELETE FROM public.genia_animal
  WHERE produtor_id IN (
    SELECT id FROM public.genia_produtor
    WHERE user_id IN (
      'c2bd0ee3-7119-412c-ada9-11fc636d40e4',
      '8a620403-5bf0-4e74-a00d-b8a78066e3f9',
      'd4b1a9c9-11aa-4edf-bf90-d35e99c3a0a4',
      'e33951d5-0493-476b-bf11-887099a2f1c2',
      '815855ba-c43e-4a83-ba36-3f093391fa3b',
      '73bab68e-c06b-4bdf-821d-8d1eb036e3e4',
      '9a0ebc06-0ef9-4d37-a6fa-9eaebe363206',
        'fbde59e0-f830-4cd9-9c71-3add833fb1eb'
    )
  );

DELETE FROM public.genia_produtor
  WHERE user_id IN (
    'c2bd0ee3-7119-412c-ada9-11fc636d40e4',
    '8a620403-5bf0-4e74-a00d-b8a78066e3f9',
    'd4b1a9c9-11aa-4edf-bf90-d35e99c3a0a4',
    'e33951d5-0493-476b-bf11-887099a2f1c2',
    '815855ba-c43e-4a83-ba36-3f093391fa3b',
    '73bab68e-c06b-4bdf-821d-8d1eb036e3e4',
    '9a0ebc06-0ef9-4d37-a6fa-9eaebe363206',
        'fbde59e0-f830-4cd9-9c71-3add833fb1eb'
  );

-- =============================================================================
-- 1. PRODUTORES
-- =============================================================================

INSERT INTO public.genia_produtor
  (id, user_id, nome, municipio, estado, nome_fazenda, hectares, telefone, cpf)
VALUES
  -- produtor1@teste.com — Bovinos de corte (Crateús)
  ('11111111-0001-0001-0001-000000000001',
   'c2bd0ee3-7119-412c-ada9-11fc636d40e4',
   'João Antônio de Sousa', 'Crateús', 'CE',
   'Fazenda Boa Esperança', 320, '(88) 99201-4455', '123.456.789-01'),

  -- produtor2@teste.com — Caprinos (Novo Oriente)
  ('11111111-0002-0002-0002-000000000002',
   '8a620403-5bf0-4e74-a00d-b8a78066e3f9',
   'Maria das Graças Silva', 'Novo Oriente', 'CE',
   'Fazenda São Francisco', 180, '(88) 98832-7762', '234.567.890-02'),

  -- produtor3@teste.com — Ovinos (Monsenhor Tabosa)
  ('11111111-0003-0003-0003-000000000003',
   'd4b1a9c9-11aa-4edf-bf90-d35e99c3a0a4',
   'Francisco Gonçalves Neto', 'Monsenhor Tabosa', 'CE',
   'Fazenda Brejinho', 240, '(88) 99543-1188', '345.678.901-03'),

  -- produtor4@teste.com — Bovinos leiteiros (Tamboril)
  ('11111111-0004-0004-0004-000000000004',
   'e33951d5-0493-476b-bf11-887099a2f1c2',
   'Ana Lúcia Ferreira Costa', 'Tamboril', 'CE',
   'Fazenda Vale Verde', 150, '(88) 98721-3399', '456.789.012-04'),

  -- produtor5@teste.com — Rebanho misto (Catunda)
  ('11111111-0005-0005-0005-000000000005',
   '815855ba-c43e-4a83-ba36-3f093391fa3b',
   'Pedro Henrique Alves Lima', 'Catunda', 'CE',
   'Fazenda Serra Alta', 410, '(88) 99654-2277', '567.890.123-05'),

  -- produtor6@teste.com — Bovinos corte (Poranga)
  ('11111111-0006-0006-0006-000000000006',
   '73bab68e-c06b-4bdf-821d-8d1eb036e3e4',
   'Raimundo Pereira do Nascimento', 'Poranga', 'CE',
   'Fazenda Cacimbão', 280, '(88) 98811-5544', '678.901.234-06'),

  -- produtor7@teste.cpm — Caprinos e Ovinos (Independência)
  ('11111111-0007-0007-0007-000000000007',
   '9a0ebc06-0ef9-4d37-a6fa-9eaebe363206',
   'Antônia Bezerra de Oliveira', 'Independência', 'CE',
   'Fazenda Lagoa Seca', 195, '(88) 99312-8866', '789.012.345-07');

-- =============================================================================
-- 2. ANIMAIS — Produtor 1 (João • Bovinos de corte • Fazenda Boa Esperança)
-- =============================================================================

INSERT INTO public.genia_animal
  (id, produtor_id, nome, especie, raca, sexo, data_nascimento,
   peso_kg, escore_corporal, fazenda,
   nome_pai, raca_pai, nome_mae, raca_mae,
   numero_partos, abortos, dias_ultimo_parto, filhos_matriz,
   qualidade_semen, filhos_macho,
   producao_leite_diaria, prenhou, disponivel_match)
VALUES
  ('a1000001-0000-0000-0000-000000000001', '11111111-0001-0001-0001-000000000001',
   'Trovão', 'bovino', 'Nelore', 'macho', '2020-03-15',
   620, 4.0, 'Fazenda Boa Esperança',
   'Herói', 'Nelore', 'Mimosa', 'Nelore',
   0, 0, 0, 0, 4.8, 22, 0, false, true),

  ('a1000002-0000-0000-0000-000000000002', '11111111-0001-0001-0001-000000000001',
   'Sultão', 'bovino', 'Angus', 'macho', '2021-07-10',
   580, 4.2, 'Fazenda Boa Esperança',
   'Campeão', 'Angus', 'Bela', 'Angus',
   0, 0, 0, 0, 4.5, 18, 0, false, true),

  ('a1000003-0000-0000-0000-000000000003', '11111111-0001-0001-0001-000000000001',
   'Frida', 'bovino', 'Nelore', 'femea', '2021-05-20',
   420, 3.5, 'Fazenda Boa Esperança',
   'Trovão', 'Nelore', 'Mimosa', 'Nelore',
   2, 0, 45, 2, 0, 0, 0, true, true),

  ('a1000004-0000-0000-0000-000000000004', '11111111-0001-0001-0001-000000000001',
   'Baleia', 'bovino', 'Nelore', 'femea', '2020-11-08',
   450, 3.8, 'Fazenda Boa Esperança',
   'Sultão', 'Angus', 'Beleza', 'Nelore',
   3, 0, 30, 3, 0, 0, 0, true, true),

  ('a1000005-0000-0000-0000-000000000005', '11111111-0001-0001-0001-000000000001',
   'Mimosa', 'bovino', 'Nelore', 'femea', '2019-08-22',
   480, 4.0, 'Fazenda Boa Esperança',
   'Herói', 'Nelore', 'Serena', 'Nelore',
   4, 1, 60, 3, 0, 0, 0, false, true),

  ('a1000006-0000-0000-0000-000000000006', '11111111-0001-0001-0001-000000000001',
   'Formosa', 'bovino', 'Angus', 'femea', '2022-02-14',
   390, 3.2, 'Fazenda Boa Esperança',
   'Sultão', 'Angus', 'Bela', 'Angus',
   1, 0, 90, 1, 0, 0, 0, false, true),

  ('a1000007-0000-0000-0000-000000000007', '11111111-0001-0001-0001-000000000001',
   'Neguinha', 'bovino', 'Nelore', 'femea', '2023-01-05',
   320, 3.0, 'Fazenda Boa Esperança',
   'Trovão', 'Nelore', 'Frida', 'Nelore',
   0, 0, 0, 0, 0, 0, 0, false, true);

-- =============================================================================
-- 3. ANIMAIS — Produtor 2 (Maria • Caprinos • Fazenda São Francisco)
-- =============================================================================

INSERT INTO public.genia_animal
  (id, produtor_id, nome, especie, raca, sexo, data_nascimento,
   peso_kg, escore_corporal, fazenda,
   nome_pai, raca_pai, nome_mae, raca_mae,
   numero_partos, abortos, dias_ultimo_parto, filhos_matriz,
   qualidade_semen, filhos_macho,
   producao_leite_diaria, prenhou, disponivel_match)
VALUES
  ('a2000001-0000-0000-0000-000000000001', '11111111-0002-0002-0002-000000000002',
   'Faroeste', 'caprino', 'Boer', 'macho', '2021-04-12',
   85, 4.5, 'Fazenda São Francisco',
   '', '', '', '',
   0, 0, 0, 0, 4.7, 30, 0, false, true),

  ('a2000002-0000-0000-0000-000000000002', '11111111-0002-0002-0002-000000000002',
   'Penélope', 'caprino', 'Anglo-Nubiana', 'femea', '2021-09-18',
   52, 3.8, 'Fazenda São Francisco',
   'Faroeste', 'Boer', 'Belinha', 'Anglo-Nubiana',
   3, 0, 40, 5, 0, 0, 2.8, true, true),

  ('a2000003-0000-0000-0000-000000000003', '11111111-0002-0002-0002-000000000002',
   'Belinha', 'caprino', 'Saanen', 'femea', '2020-06-30',
   58, 4.0, 'Fazenda São Francisco',
   '', '', '', '',
   4, 0, 55, 7, 0, 0, 3.5, true, true),

  ('a2000004-0000-0000-0000-000000000004', '11111111-0002-0002-0002-000000000002',
   'Cheirosa', 'caprino', 'Anglo-Nubiana', 'femea', '2022-03-10',
   44, 3.5, 'Fazenda São Francisco',
   'Faroeste', 'Boer', 'Penélope', 'Anglo-Nubiana',
   1, 0, 70, 2, 0, 0, 2.2, false, true),

  ('a2000005-0000-0000-0000-000000000005', '11111111-0002-0002-0002-000000000002',
   'Rajado', 'caprino', 'Boer', 'macho', '2022-11-25',
   72, 4.0, 'Fazenda São Francisco',
   'Faroeste', 'Boer', 'Belinha', 'Saanen',
   0, 0, 0, 0, 4.2, 8, 0, false, false),

  ('a2000006-0000-0000-0000-000000000006', '11111111-0002-0002-0002-000000000002',
   'Malhada', 'caprino', 'Moxotó', 'femea', '2021-08-05',
   46, 3.7, 'Fazenda São Francisco',
   '', '', '', '',
   2, 1, 85, 3, 0, 0, 1.9, false, true);

-- =============================================================================
-- 4. ANIMAIS — Produtor 3 (Francisco • Ovinos • Fazenda Brejinho)
-- =============================================================================

INSERT INTO public.genia_animal
  (id, produtor_id, nome, especie, raca, sexo, data_nascimento,
   peso_kg, escore_corporal, fazenda,
   nome_pai, raca_pai, nome_mae, raca_mae,
   numero_partos, abortos, dias_ultimo_parto, filhos_matriz,
   qualidade_semen, filhos_macho,
   producao_leite_diaria, prenhou, disponivel_match)
VALUES
  ('a3000001-0000-0000-0000-000000000001', '11111111-0003-0003-0003-000000000003',
   'Leão', 'ovino', 'Dorper', 'macho', '2021-02-20',
   95, 4.3, 'Fazenda Brejinho',
   '', 'Dorper', '', 'Dorper',
   0, 0, 0, 0, 4.6, 25, 0, false, true),

  ('a3000002-0000-0000-0000-000000000002', '11111111-0003-0003-0003-000000000003',
   'Rosinha', 'ovino', 'Santa Inês', 'femea', '2021-06-15',
   55, 3.9, 'Fazenda Brejinho',
   'Leão', 'Dorper', 'Flor', 'Santa Inês',
   3, 0, 35, 4, 0, 0, 0, true, true),

  ('a3000003-0000-0000-0000-000000000003', '11111111-0003-0003-0003-000000000003',
   'Flor', 'ovino', 'Santa Inês', 'femea', '2020-09-12',
   60, 4.1, 'Fazenda Brejinho',
   '', '', '', '',
   4, 0, 50, 6, 0, 0, 0, true, true),

  ('a3000004-0000-0000-0000-000000000004', '11111111-0003-0003-0003-000000000003',
   'Neve', 'ovino', 'Texel', 'femea', '2022-04-08',
   48, 3.5, 'Fazenda Brejinho',
   'Leão', 'Dorper', 'Rosinha', 'Santa Inês',
   1, 0, 80, 1, 0, 0, 0, false, true),

  ('a3000005-0000-0000-0000-000000000005', '11111111-0003-0003-0003-000000000003',
   'Gigante', 'ovino', 'Dorper', 'macho', '2022-10-30',
   88, 4.0, 'Fazenda Brejinho',
   'Leão', 'Dorper', 'Flor', 'Santa Inês',
   0, 0, 0, 0, 4.4, 12, 0, false, true),

  ('a3000006-0000-0000-0000-000000000006', '11111111-0003-0003-0003-000000000003',
   'Pintada', 'ovino', 'Morada Nova', 'femea', '2021-12-20',
   50, 3.7, 'Fazenda Brejinho',
   '', '', '', '',
   2, 0, 65, 3, 0, 0, 0, false, true),

  ('a3000007-0000-0000-0000-000000000007', '11111111-0003-0003-0003-000000000003',
   'Branca', 'ovino', 'Santa Inês', 'femea', '2023-03-14',
   38, 3.0, 'Fazenda Brejinho',
   'Leão', 'Dorper', 'Neve', 'Texel',
   0, 0, 0, 0, 0, 0, 0, false, false);

-- =============================================================================
-- 5. ANIMAIS — Produtor 4 (Ana Lúcia • Bovinos leiteiros • Fazenda Vale Verde)
-- =============================================================================

INSERT INTO public.genia_animal
  (id, produtor_id, nome, especie, raca, sexo, data_nascimento,
   peso_kg, escore_corporal, fazenda,
   nome_pai, raca_pai, nome_mae, raca_mae,
   numero_partos, abortos, dias_ultimo_parto, filhos_matriz,
   qualidade_semen, filhos_macho,
   producao_leite_diaria, prenhou, disponivel_match)
VALUES
  ('a4000001-0000-0000-0000-000000000001', '11111111-0004-0004-0004-000000000004',
   'Leite Puro', 'bovino', 'Girolando', 'macho', '2020-05-18',
   520, 4.0, 'Fazenda Vale Verde',
   'Campeão', 'Holandês', 'Estrela', 'Gir',
   0, 0, 0, 0, 4.6, 35, 0, false, true),

  ('a4000002-0000-0000-0000-000000000002', '11111111-0004-0004-0004-000000000004',
   'Estrela', 'bovino', 'Gir', 'femea', '2019-10-25',
   440, 3.8, 'Fazenda Vale Verde',
   '', 'Gir', '', 'Gir',
   5, 0, 25, 5, 0, 0, 18.5, true, true),

  ('a4000003-0000-0000-0000-000000000003', '11111111-0004-0004-0004-000000000004',
   'Bonança', 'bovino', 'Girolando', 'femea', '2021-01-14',
   420, 3.6, 'Fazenda Vale Verde',
   'Leite Puro', 'Girolando', 'Estrela', 'Gir',
   3, 0, 38, 3, 0, 0, 14.2, true, true),

  ('a4000004-0000-0000-0000-000000000004', '11111111-0004-0004-0004-000000000004',
   'Serena', 'bovino', 'Holandês', 'femea', '2020-07-30',
   460, 4.0, 'Fazenda Vale Verde',
   '', 'Holandês', '', 'Holandês',
   4, 1, 45, 3, 0, 0, 22.0, false, true),

  ('a4000005-0000-0000-0000-000000000005', '11111111-0004-0004-0004-000000000004',
   'Leitosa', 'bovino', 'Girolando', 'femea', '2022-08-11',
   380, 3.4, 'Fazenda Vale Verde',
   'Leite Puro', 'Girolando', 'Bonança', 'Girolando',
   1, 0, 95, 1, 0, 0, 10.8, false, true),

  ('a4000006-0000-0000-0000-000000000006', '11111111-0004-0004-0004-000000000004',
   'Clarinha', 'bovino', 'Gir', 'femea', '2023-02-28',
   310, 3.0, 'Fazenda Vale Verde',
   'Leite Puro', 'Girolando', 'Serena', 'Holandês',
   0, 0, 0, 0, 0, 0, 0, false, true);

-- =============================================================================
-- 6. ANIMAIS — Produtor 5 (Pedro • Misto • Fazenda Serra Alta)
-- =============================================================================

INSERT INTO public.genia_animal
  (id, produtor_id, nome, especie, raca, sexo, data_nascimento,
   peso_kg, escore_corporal, fazenda,
   nome_pai, raca_pai, nome_mae, raca_mae,
   numero_partos, abortos, dias_ultimo_parto, filhos_matriz,
   qualidade_semen, filhos_macho,
   producao_leite_diaria, prenhou, disponivel_match)
VALUES
  ('a5000001-0000-0000-0000-000000000001', '11111111-0005-0005-0005-000000000005',
   'Imperador', 'bovino', 'Nelore', 'macho', '2019-11-05',
   680, 4.5, 'Fazenda Serra Alta',
   '', 'Nelore', '', 'Nelore',
   0, 0, 0, 0, 4.9, 42, 0, false, true),

  ('a5000002-0000-0000-0000-000000000002', '11111111-0005-0005-0005-000000000005',
   'Esperança', 'bovino', 'Nelore', 'femea', '2021-03-22',
   400, 3.7, 'Fazenda Serra Alta',
   'Imperador', 'Nelore', 'Vitória', 'Nelore',
   2, 0, 50, 2, 0, 0, 0, true, true),

  ('a5000003-0000-0000-0000-000000000003', '11111111-0005-0005-0005-000000000005',
   'Bandido', 'caprino', 'Boer', 'macho', '2021-08-17',
   82, 4.2, 'Fazenda Serra Alta',
   '', 'Boer', '', 'Boer',
   0, 0, 0, 0, 4.5, 20, 0, false, true),

  ('a5000004-0000-0000-0000-000000000004', '11111111-0005-0005-0005-000000000005',
   'Docinha', 'caprino', 'Anglo-Nubiana', 'femea', '2022-01-09',
   48, 3.8, 'Fazenda Serra Alta',
   'Bandido', 'Boer', 'Cabrita', 'Anglo-Nubiana',
   2, 0, 60, 3, 0, 0, 2.4, true, true),

  ('a5000005-0000-0000-0000-000000000005', '11111111-0005-0005-0005-000000000005',
   'Valente', 'ovino', 'Dorper', 'macho', '2022-05-30',
   90, 4.1, 'Fazenda Serra Alta',
   '', 'Dorper', '', 'Dorper',
   0, 0, 0, 0, 4.3, 15, 0, false, true),

  ('a5000006-0000-0000-0000-000000000006', '11111111-0005-0005-0005-000000000005',
   'Vitória', 'bovino', 'Nelore', 'femea', '2020-04-18',
   430, 4.0, 'Fazenda Serra Alta',
   '', '', '', '',
   3, 0, 40, 3, 0, 0, 0, false, true),

  ('a5000007-0000-0000-0000-000000000007', '11111111-0005-0005-0005-000000000005',
   'Cabrita', 'caprino', 'Anglo-Nubiana', 'femea', '2020-12-01',
   55, 3.9, 'Fazenda Serra Alta',
   '', '', '', '',
   4, 0, 30, 6, 0, 0, 3.1, true, true),

  ('a5000008-0000-0000-0000-000000000008', '11111111-0005-0005-0005-000000000005',
   'Nobre', 'bovino', 'Angus', 'macho', '2022-09-14',
   550, 4.2, 'Fazenda Serra Alta',
   'Imperador', 'Nelore', 'Esperança', 'Nelore',
   0, 0, 0, 0, 4.0, 6, 0, false, true);

-- =============================================================================
-- 7. ANIMAIS — Produtor 6 (Raimundo • Bovinos corte • Fazenda Cacimbão)
-- =============================================================================

INSERT INTO public.genia_animal
  (id, produtor_id, nome, especie, raca, sexo, data_nascimento,
   peso_kg, escore_corporal, fazenda,
   nome_pai, raca_pai, nome_mae, raca_mae,
   numero_partos, abortos, dias_ultimo_parto, filhos_matriz,
   qualidade_semen, filhos_macho,
   producao_leite_diaria, prenhou, disponivel_match)
VALUES
  ('a6000001-0000-0000-0000-000000000001', '11111111-0006-0006-0006-000000000006',
   'Xerifão', 'bovino', 'Angus', 'macho', '2020-01-15',
   650, 4.6, 'Fazenda Cacimbão',
   '', 'Angus', '', 'Angus',
   0, 0, 0, 0, 4.8, 38, 0, false, true),

  ('a6000002-0000-0000-0000-000000000002', '11111111-0006-0006-0006-000000000006',
   'Tereza', 'bovino', 'Nelore', 'femea', '2021-07-22',
   410, 3.6, 'Fazenda Cacimbão',
   'Xerifão', 'Angus', 'Princesa', 'Nelore',
   2, 0, 55, 2, 0, 0, 0, true, true),

  ('a6000003-0000-0000-0000-000000000003', '11111111-0006-0006-0006-000000000006',
   'Princesa', 'bovino', 'Nelore', 'femea', '2020-03-08',
   445, 3.9, 'Fazenda Cacimbão',
   '', '', '', '',
   3, 1, 70, 2, 0, 0, 0, false, true),

  ('a6000004-0000-0000-0000-000000000004', '11111111-0006-0006-0006-000000000006',
   'Fortuna', 'bovino', 'Angus', 'femea', '2022-06-19',
   370, 3.3, 'Fazenda Cacimbão',
   'Xerifão', 'Angus', 'Tereza', 'Nelore',
   1, 0, 100, 1, 0, 0, 0, false, true),

  ('a6000005-0000-0000-0000-000000000005', '11111111-0006-0006-0006-000000000006',
   'Bombom', 'bovino', 'Nelore', 'femea', '2023-04-05',
   290, 2.9, 'Fazenda Cacimbão',
   'Xerifão', 'Angus', 'Princesa', 'Nelore',
   0, 0, 0, 0, 0, 0, 0, false, false);

-- =============================================================================
-- 8. ANIMAIS — Produtor 7 (Antônia • Caprinos/Ovinos • Fazenda Lagoa Seca)
-- =============================================================================

INSERT INTO public.genia_animal
  (id, produtor_id, nome, especie, raca, sexo, data_nascimento,
   peso_kg, escore_corporal, fazenda,
   nome_pai, raca_pai, nome_mae, raca_mae,
   numero_partos, abortos, dias_ultimo_parto, filhos_matriz,
   qualidade_semen, filhos_macho,
   producao_leite_diaria, prenhou, disponivel_match)
VALUES
  ('a7000001-0000-0000-0000-000000000001', '11111111-0007-0007-0007-000000000007',
   'Valentão', 'caprino', 'Boer', 'macho', '2021-05-10',
   88, 4.4, 'Fazenda Lagoa Seca',
   '', 'Boer', '', 'Boer',
   0, 0, 0, 0, 4.7, 28, 0, false, true),

  ('a7000002-0000-0000-0000-000000000002', '11111111-0007-0007-0007-000000000007',
   'Joaninha', 'caprino', 'Saanen', 'femea', '2021-10-28',
   54, 4.0, 'Fazenda Lagoa Seca',
   'Valentão', 'Boer', 'Querida', 'Saanen',
   3, 0, 42, 4, 0, 0, 3.8, true, true),

  ('a7000003-0000-0000-0000-000000000003', '11111111-0007-0007-0007-000000000007',
   'Querida', 'caprino', 'Saanen', 'femea', '2020-07-14',
   60, 4.2, 'Fazenda Lagoa Seca',
   '', '', '', '',
   4, 0, 35, 7, 0, 0, 4.2, true, true),

  ('a7000004-0000-0000-0000-000000000004', '11111111-0007-0007-0007-000000000007',
   'Campeão', 'ovino', 'Dorper', 'macho', '2022-02-18',
   92, 4.3, 'Fazenda Lagoa Seca',
   '', 'Dorper', '', 'Dorper',
   0, 0, 0, 0, 4.5, 16, 0, false, true),

  ('a7000005-0000-0000-0000-000000000005', '11111111-0007-0007-0007-000000000007',
   'Lindinha', 'ovino', 'Santa Inês', 'femea', '2022-08-22',
   52, 3.7, 'Fazenda Lagoa Seca',
   'Campeão', 'Dorper', 'Bonita', 'Santa Inês',
   1, 0, 75, 2, 0, 0, 0, false, true),

  ('a7000006-0000-0000-0000-000000000006', '11111111-0007-0007-0007-000000000007',
   'Bonita', 'ovino', 'Santa Inês', 'femea', '2021-04-03',
   57, 3.9, 'Fazenda Lagoa Seca',
   '', '', '', '',
   2, 0, 55, 3, 0, 0, 0, true, true),

  ('a7000007-0000-0000-0000-000000000007', '11111111-0007-0007-0007-000000000007',
   'Miúda', 'caprino', 'Moxotó', 'femea', '2022-11-11',
   42, 3.5, 'Fazenda Lagoa Seca',
   'Valentão', 'Boer', 'Joaninha', 'Saanen',
   1, 0, 90, 1, 0, 0, 2.0, false, true);

-- =============================================================================
-- PRODUTOR MARIANA (marianalemos595@gmail.com — fbde59e0-f830-4cd9-9c71-3add833fb1eb)
-- Conta já existia no Auth mas sem perfil em genia_produtor — corrigido aqui
-- =============================================================================

INSERT INTO public.genia_produtor
  (id, user_id, nome, municipio, estado, nome_fazenda, hectares, telefone)
VALUES
  ('11111111-0008-0008-0008-000000000008',
   'fbde59e0-f830-4cd9-9c71-3add833fb1eb',
   'Mariana Lemos', 'Crateús', 'CE',
   'Fazenda Santa Mariana', 120, '(88) 99100-0000');

INSERT INTO public.genia_animal
  (id, produtor_id, nome, especie, raca, sexo, data_nascimento,
   peso_kg, escore_corporal, fazenda,
   numero_partos, abortos, dias_ultimo_parto, filhos_matriz,
   qualidade_semen, filhos_macho, producao_leite_diaria, prenhou, disponivel_match)
VALUES
  ('a8000001-0000-0000-0000-000000000001', '11111111-0008-0008-0008-000000000008',
   'Aurora', 'bovino', 'Girolando', 'femea', '2021-06-15',
   400, 3.7, 'Fazenda Santa Mariana',
   2, 0, 50, 2, 0, 0, 12.5, true, true),

  ('a8000002-0000-0000-0000-000000000002', '11111111-0008-0008-0008-000000000008',
   'Trovador', 'bovino', 'Gir', 'macho', '2020-09-20',
   510, 4.2, 'Fazenda Santa Mariana',
   0, 0, 0, 0, 4.5, 18, 0, false, true),

  ('a8000003-0000-0000-0000-000000000003', '11111111-0008-0008-0008-000000000008',
   'Clarinha', 'bovino', 'Girolando', 'femea', '2022-04-10',
   360, 3.4, 'Fazenda Santa Mariana',
   1, 0, 80, 1, 0, 0, 8.0, false, true);

-- 11. Reabilita RLS com policies corretas
-- =============================================================================

ALTER TABLE public.genia_produtor           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_animal             ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.genia_evento_reprodutivo ENABLE ROW LEVEL SECURITY;

-- =============================================================================
-- RESUMO DO SEED
-- Produtor 1 — João Antônio      : 7 bovinos (Nelore/Angus corte) — Crateús
-- Produtor 2 — Maria das Graças  : 6 caprinos (Boer/Anglo/Saanen) — Novo Oriente
-- Produtor 3 — Francisco Neto    : 7 ovinos (Dorper/Santa Inês)   — Mon. Tabosa
-- Produtor 4 — Ana Lúcia         : 6 bovinos leiteiros (Girolando) — Tamboril
-- Produtor 5 — Pedro Henrique    : 8 animais mistos               — Catunda
-- Produtor 6 — Raimundo Pereira  : 5 bovinos (Angus/Nelore corte) — Poranga
-- Produtor 7 — Antônia Bezerra   : 7 caprinos/ovinos              — Independência
-- Produtor 8 — Mariana Lemos     : 3 bovinos leiteiros            — Crateús
--
-- Logins de demonstração (senha: Teste@123):
--   produtor1@teste.com  produtor2@teste.com  produtor3@teste.com
--   produtor4@teste.com  produtor5@teste.com  produtor6@teste.com
-- Conta já existente:
--   marianalemos595@gmail.com  (perfil criado por este seed)
--   produtor7@teste.cpm
-- =============================================================================
