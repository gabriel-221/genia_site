## Table `genia_produtor`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `user_id` | `uuid` |  Nullable |
| `nome` | `text` |  |
| `cpf` | `text` |  Nullable Unique |
| `telefone` | `text` |  Nullable |
| `municipio` | `text` |  |
| `estado` | `text` |  |
| `nome_fazenda` | `text` |  Nullable |
| `hectares` | `numeric` |  Nullable |
| `foto_url` | `text` |  Nullable |
| `ativo` | `bool` |  |
| `criado_em` | `timestamptz` |  |
| `atualizado_em` | `timestamptz` |  |

## Table `genia_animal`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `produtor_id` | `uuid` |  |
| `nome` | `text` |  |
| `especie` | `genia_especie_enum` |  |
| `raca` | `text` |  |
| `linhagem` | `text` |  Nullable |
| `sexo` | `genia_sexo_enum` |  |
| `data_nascimento` | `date` |  Nullable |
| `rfid_tag` | `text` |  Nullable Unique |
| `slug_publico` | `text` |  Nullable Unique |
| `peso_kg` | `numeric` |  Nullable |
| `escore_corporal` | `numeric` |  Nullable |
| `coef_endogamia` | `numeric` |  Nullable |
| `foto_url` | `text` |  Nullable |
| `ativo` | `bool` |  |
| `criado_em` | `timestamptz` |  |
| `atualizado_em` | `timestamptz` |  |
| `fazenda` | `text` |  |
| `nome_pai` | `text` |  |
| `raca_pai` | `text` |  |
| `rfid_pai` | `text` |  |
| `nome_mae` | `text` |  |
| `raca_mae` | `text` |  |
| `rfid_mae` | `text` |  |
| `numero_partos` | `int4` |  |
| `abortos` | `int4` |  |
| `dias_ultimo_parto` | `int4` |  |
| `filhos_matriz` | `int4` |  |
| `qualidade_semen` | `numeric` |  |
| `filhos_macho` | `int4` |  |
| `chance_prenhez` | `numeric` |  Nullable |
| `prenhou` | `bool` |  |
| `nfc_ativo` | `bool` |  |
| `nfc_gravada_em` | `timestamptz` |  Nullable |
| `disponivel_match` | `bool` |  |
| `producao_leite_diaria` | `numeric` |  |

## Table `genia_pedigree`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `animal_id` | `uuid` |  |
| `pai_id` | `uuid` |  Nullable |
| `mae_id` | `uuid` |  Nullable |
| `geracao` | `int4` |  |
| `criado_em` | `timestamptz` |  |

## Table `genia_semen`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `produtor_id` | `uuid` |  |
| `nome_reprodutor` | `text` |  |
| `raca` | `text` |  |
| `especie` | `genia_especie_enum` |  |
| `registro` | `text` |  Nullable |
| `motilidade_pct` | `numeric` |  Nullable |
| `concentracao` | `int4` |  Nullable |
| `origem` | `text` |  Nullable |
| `preco_dose` | `numeric` |  Nullable |
| `estoque_doses` | `int4` |  Nullable |
| `ativo` | `bool` |  |
| `criado_em` | `timestamptz` |  |

## Table `genia_ciclo_cio`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `animal_id` | `uuid` |  |
| `data_deteccao` | `date` |  |
| `proxima_previsao` | `date` |  Nullable |
| `inseminado` | `bool` |  |
| `observacoes` | `text` |  Nullable |
| `criado_em` | `timestamptz` |  |

## Table `genia_evento_reprodutivo`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `animal_id` | `uuid` |  |
| `tipo` | `genia_evento_tipo_enum` |  |
| `data_evento` | `date` |  |
| `semen_id` | `uuid` |  Nullable |
| `tecnico_responsavel` | `text` |  Nullable |
| `gestacao_confirmada` | `bool` |  Nullable |
| `data_parto_previsto` | `date` |  Nullable |
| `score_ia_prenhez` | `numeric` |  Nullable |
| `fatores_ia` | `jsonb` |  Nullable |
| `observacoes` | `text` |  Nullable |
| `criado_em` | `timestamptz` |  |
| `semen_reprodutor` | `text` |  |

## Table `genia_gene_match`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `animal_a_id` | `uuid` |  |
| `animal_b_id` | `uuid` |  |
| `produtor_solicitante_id` | `uuid` |  |
| `score_compatibilidade` | `numeric` |  |
| `coef_parentesco` | `numeric` |  Nullable |
| `dgc_estimado` | `numeric` |  Nullable |
| `score_peso_prole` | `numeric` |  Nullable |
| `status` | `genia_match_status_enum` |  |
| `criado_em` | `timestamptz` |  |
| `atualizado_em` | `timestamptz` |  |

## Table `genia_genetico_alerta`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `id` | `uuid` | Primary |
| `animal_a_id` | `uuid` |  |
| `animal_b_id` | `uuid` |  |
| `nivel_risco` | `genia_risco_nivel_enum` |  |
| `coef_parentesco` | `numeric` |  Nullable |
| `descricao` | `text` |  |
| `resolvido` | `bool` |  |
| `criado_em` | `timestamptz` |  |

