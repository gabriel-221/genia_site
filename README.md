# G.E.N.I.A — Gestão Genética Inteligente de Rebanhos

> **Hackathon Expoagro Crateús 2026** — Desafio: *Sistema de Coleta de Dados Genéticos para Monitoramento de Inseminação Artificial em Bovinos, Ovinos e Caprinos*

[![Licença: GPL v3](https://img.shields.io/badge/Licen%C3%A7a-GPL%20v3-blue.svg)](LICENSE)
[![Next.js](https://img.shields.io/badge/Next.js-14-black?logo=next.js)](https://nextjs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?logo=typescript)](https://www.typescriptlang.org)
[![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-green?logo=supabase)](https://supabase.com)
[![Deploy: Vercel](https://img.shields.io/badge/Deploy-Vercel-black?logo=vercel)](https://vercel.com)

Plataforma web progressiva (PWA) para gestão genética e reprodutiva de rebanhos bovinos, ovinos e caprinos. O GENIA integra inteligência artificial embarcada, rastreabilidade por NFC e um assistente conversacional com LLM para empoderar produtores rurais do Sertão nordestino.

---

## Sumário

- [Estrutura do Repositório](#estrutura-do-repositório)
- [Atendimento ao Edital](#atendimento-ao-edital)
- [Tecnologias e Frameworks](#tecnologias-e-frameworks)
- [Inteligência Artificial — Implementação](#inteligência-artificial--implementação)
- [Conformidade com a LGPD](#conformidade-com-a-lgpd)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do Banco de Dados](#configuração-do-banco-de-dados-supabase)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Instalação e Execução Local](#instalação-e-execução-local)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Banco de Dados — Referência](#banco-de-dados--referência)
- [Deploy em Produção](#deploy-em-produção)
- [Licença](#licença)

---

## Estrutura do Repositório

Este repositório centraliza **os três componentes independentes do GENIA** em branches dedicadas:

| Branch | Componente | Stack Principal | Descrição |
|--------|------------|-----------------|-----------|
| [`master`](../../tree/master) | **Web PWA** | Next.js 14 · TypeScript · Supabase | Interface web responsiva, API serverless, GENIA Copilot (Claude Haiku) e painel de gestão |
| [`genia-apk`](../../tree/genia-apk) | **App Android** | Kotlin · Jetpack Compose · Room · ONNX Runtime | Aplicativo Android nativo com NFC, modelo preditivo embarcado e sincronização offline |
| [`data-and-ai`](../../tree/data-and-ai) | **IA & Dados** | Python · scikit-learn · ONNX · pandas | Pipeline de geração de dataset sintético, treinamento do Random Forest, exportação ONNX e relatórios |

> Cada branch possui seu próprio `README.md` com instruções específicas de instalação e uso.

---

## Atendimento ao Edital

| Requisito do Edital | Como o GENIA atende (Web PWA) | Status |
|---------------------|-------------------------------|--------|
| Cadastro e gestão de animais com dados genéticos por espécie | Wizard de cadastro com espécie, raça validada, pedigree (pai/mãe), escores corporais e histórico reprodutivo completo | ✅ |
| Registro e monitoramento de ciclos reprodutivos e eventos de IA por espécie | Módulo de eventos com tipos: Cio, Inseminação, Diagnóstico, Parto e Desmame; calendário visual mensal interativo | ✅ |
| Análise preditiva por IA para estimar taxas de prenhez | Modelo Random Forest (treinado em Python, exportado para ONNX e reimplementado em TypeScript) com 15 variáveis preditoras | ✅ |
| Geração de relatórios de desempenho genético e reprodutivo | Dashboard com KPIs (taxa de prenhez, IEP, distribuição por espécie) e exportação em CSV | ✅ |
| Recomendações por IA para seleção de reprodutores | GeneMatch: algoritmo proprietário de ranqueamento genético por objetivo (Leite, Corte ou Fertilidade) com fatores explicativos | ✅ |
| Interface acessível para técnicos e produtores rurais, inclusive mobile | PWA responsiva com navegação bottom-tab, otimizada para smartphones; suporte a acessibilidade com VLibras (Língua de Sinais) | ✅ |
| Armazenamento seguro com exportação e integração com rastreabilidade | Supabase PostgreSQL com RLS por produtor; tag NFC para rastreabilidade pública por animal (`/a/[nfcCode]`) | ✅ |
| IA para predição de resultados de inseminação | `/api/prenhez` com Random Forest: ECC, idade, peso, dias pós-parto, qualidade seminal, parentesco e 9 outras variáveis | ✅ |
| Identificação de padrões de fertilidade e desempenho | Dashboard com taxa de prenhez por rebanho, alertas de cio previsto e histórico reprodutivo por animal | ✅ |
| Recomendação automatizada de fêmeas e machos por critérios genéticos | GENIA Copilot: assistente conversacional LLM (Claude Haiku) com contexto do rebanho real do produtor | ✅ |
| Software livre licenciado sob GNU GPL v3.0 | Repositório público com licença [GNU GPL v3.0](LICENSE) | ✅ |

---

## Tecnologias e Frameworks

### Web PWA — branch `master` (este repositório)

| Camada | Tecnologia | Versão | Finalidade |
|--------|-----------|--------|-----------|
| Framework | [Next.js](https://nextjs.org) | 14.2 | App Router, Server Components, API Routes serverless |
| Linguagem | [TypeScript](https://www.typescriptlang.org) | 5.x | Tipagem estática em todo o codebase |
| Estilização | [Tailwind CSS](https://tailwindcss.com) | 3.4 | Utilitários CSS, design system consistente |
| Banco de Dados | [Supabase](https://supabase.com) | 2.x | PostgreSQL gerenciado, Auth, Row Level Security |
| IA Conversacional | [Anthropic Claude Haiku](https://www.anthropic.com) | haiku-4-5 | GENIA Copilot — respostas em linguagem natural sobre o rebanho |
| Deploy | [Vercel](https://vercel.com) | — | Edge Network, deploy contínuo via GitHub |
| PWA | Web App Manifest + Service Worker | — | Instalável em dispositivos móveis, ícones nativos |
| Acessibilidade | [VLibras](https://vlibras.gov.br) | — | Tradução automática para Libras (Gov.BR) |
| Segurança | Cookie-based session, rate limiting, sanitização | — | Proteção das API routes, middleware de autenticação |

### App Android — branch `genia-apk`

Kotlin · Jetpack Compose · Room Database · ONNX Runtime · Supabase Kotlin SDK · NFC · Material Design 3

### IA & Dados — branch `data-and-ai`

Python 3.11 · scikit-learn · pandas · NumPy · ONNX / skl2onnx · ONNX Runtime · matplotlib · seaborn · Jupyter

---

## Inteligência Artificial — Implementação

O GENIA incorpora três camadas distintas de inteligência artificial:

### 1. Predição de Prenhez — Random Forest (ONNX)

**Pipeline completo** (branch `data-and-ai`):

```
data/raw/dataset.csv          ← gerado por src/generate_dataset.py (10.000 amostras sintéticas)
         │
         ▼
src/train_random_forest.py    ← treinamento scikit-learn RandomForestClassifier
         │
         ▼
models/random_forest_prenhez.onnx  ← exportação via skl2onnx
         │
    ┌────┴────────────────────┐
    │                         │
    ▼                         ▼
Android (ONNX Runtime)    Web API (TypeScript)
app/.../inference          app/api/prenhez/route.ts
```

**15 variáveis preditoras:**

| Variável | Tipo | Descrição |
|----------|------|-----------|
| `especie` | string | Bovino / Ovino / Caprino |
| `raca_matriz` | string | Raça da fêmea |
| `idade_matriz` | float | Idade em anos |
| `peso_matriz_kg` | float | Peso corporal (kg) |
| `ecc_matriz` | float | Escore de Condição Corporal (1–5) |
| `numero_partos_matriz` | int | Número de partos anteriores |
| `abortos_matriz` | int | Histórico de abortos |
| `dias_desde_ultimo_parto` | int | Intervalo pós-parto (dias) |
| `filhos_nascidos_matriz` | int | Total de filhos nascidos |
| `raca_macho` | string | Raça do reprodutor |
| `idade_macho` | float | Idade em anos |
| `peso_macho_kg` | float | Peso corporal (kg) |
| `qualidade_semen_macho` | float | Score de qualidade seminal (1–5) |
| `filhos_nascidos_macho` | int | Total de filhos do reprodutor |
| `parentesco_endogamia` | float | Coeficiente de parentesco (0–1) |

**Na Web PWA**, o modelo foi reimplementado como função determinística em TypeScript (`app/api/prenhez/route.ts`), replicando exatamente os pesos e thresholds do Random Forest — sem dependência de runtime externo no servidor.

**No Android**, o arquivo `.onnx` roda diretamente no dispositivo via ONNX Runtime, permitindo inferência **100% offline**.

### 2. GeneMatch — Ranqueamento Genético Proprietário

Implementado em `lib/genetic-ranking.ts`. O algoritmo pontua cada animal do rebanho com base em fatores ponderados por objetivo:

- **Leite**: prioriza produção leiteira diária, raças leiteiras (Girolando, Gir, Saanen), ECC e histórico de partos
- **Corte**: prioriza peso corporal, ganho de peso estimado e raças de corte (Nelore, Angus, Brahman)
- **Fertilidade**: prioriza taxa de prenhez histórica, intervalo entre partos e qualidade seminal

O resultado é um ranking ordenado com classificações (`Elite genética`, `Alto potencial`, `Bom desempenho`) e fatores explicativos para cada animal.

### 3. GENIA Copilot — Assistente Conversacional LLM

**Implementação** (`app/api/copilot/route.ts`):

```
Produtor → Input (chat)
       │
       ▼
/api/copilot  ←── Auth verificada (Supabase Bearer token)
       │       ←── Rate limit: 20 req/min por usuário
       │       ←── Input sanitizado (max 1.000 chars)
       │
       ▼
Anthropic Claude Haiku API
  System prompt = contexto do rebanho real (espécie, raça, gestantes, GeneMatch)
  + instruções para resposta em PT-BR sem Markdown
       │
       ▼
Resposta em linguagem natural (≤ 3 parágrafos)
```

O contexto do rebanho é construído dinamicamente em `buildResumo()` (copilot/page.tsx) com dados reais do Supabase do produtor autenticado, permitindo recomendações personalizadas sobre **seu** plantel específico.

---

## Conformidade com a LGPD

A Lei Geral de Proteção de Dados (Lei nº 13.709/2018) foi considerada em todas as decisões arquiteturais do GENIA:

| Princípio LGPD | Implementação técnica |
|----------------|----------------------|
| **Finalidade** | Dados coletados exclusivamente para gestão do rebanho do próprio produtor. Nenhum dado é compartilhado com terceiros ou utilizado para outros fins. |
| **Adequação e Necessidade** | Apenas os campos estritamente necessários para gestão genética/reprodutiva são coletados. Não há coleta de dados sensíveis além do CPF (opcional). |
| **Segurança** | Row Level Security (RLS) no PostgreSQL: cada produtor acessa exclusivamente seus próprios dados. Sessões via cookie HttpOnly com expiração de 7 dias. Rate limiting nas APIs. Autenticação obrigatória em todas as rotas protegidas. |
| **Transparência** | Aviso de privacidade exibido na tela de login. Informação clara sobre uso e armazenamento dos dados. |
| **Titularidade** | O produtor é o único titular dos seus dados. Pode editar seu perfil e dados do rebanho a qualquer momento na tela "Mais". |
| **Não discriminação** | Os dados não são utilizados para criação de perfis discriminatórios ou venda de informações. |
| **Responsabilidade** | Código aberto sob GPL v3.0, permitindo auditoria pública. Sem trackers de terceiros ou analytics invasivos. |

**Armazenamento:** Supabase com datacenter na região `sa-east-1` (São Paulo, Brasil), garantindo soberania dos dados em território nacional.

**Telefone dos produtores:** Carregado sob demanda apenas quando o usuário clica em "Entrar em contato" no GeneMatch — não exposto em listagens bulk.

---

## Pré-requisitos

- [Node.js](https://nodejs.org) ≥ 18.17
- [npm](https://npmjs.com) ≥ 9 (ou `yarn` / `pnpm`)
- Conta no [Supabase](https://supabase.com) (plano gratuito é suficiente)
- Chave de API da [Anthropic](https://console.anthropic.com) (para o GENIA Copilot)

---

## Configuração do Banco de Dados (Supabase)

### 1. Criar um projeto Supabase

1. Acesse [supabase.com](https://supabase.com) → **New Project**
2. Defina nome, senha do banco e região (recomendado: **South America — São Paulo**)
3. Aguarde o provisionamento (≈ 2 min)

### 2. Executar o schema

1. No painel do Supabase, acesse **SQL Editor → New Query**
2. Cole o conteúdo de [`supabase/schema.sql`](supabase/schema.sql)
3. Execute (`Ctrl+Enter` ou botão **Run**)

O script cria:
- Enums de domínio (`genia_especie_enum`, `genia_sexo_enum`, etc.)
- 8 tabelas com chaves primárias UUID
- Índices de performance
- Políticas de Row Level Security (RLS)

### 3. Dados de demonstração (opcional)

Para popular o banco com dados de teste:

```sql
-- Execute no SQL Editor após o schema
-- Cole o conteúdo de supabase/seed_test.sql
```

### 4. Obter as credenciais

No painel do Supabase: **Project Settings → API**

- **URL do Projeto**: `https://<project-ref>.supabase.co`
- **Anon/Public Key**: chave pública para uso no frontend

---

## Variáveis de Ambiente

Crie um arquivo `.env.local` na raiz do projeto (este arquivo está no `.gitignore` e **nunca deve ser commitado**):

```dotenv
# Supabase — obter em: Project Settings → API
NEXT_PUBLIC_SUPABASE_URL=https://<seu-project-ref>.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=<sua-anon-key>

# Anthropic — obter em: console.anthropic.com
# Necessário para o GENIA Copilot
ANTHROPIC_API_KEY=sk-ant-api03-...
```

> **Atenção:** O prefixo `NEXT_PUBLIC_` torna a variável acessível no browser. A `ANTHROPIC_API_KEY` **não** tem esse prefixo e permanece exclusivamente no servidor.

Consulte [`.env.local.example`](.env.local.example) para um template preenchível.

---

## Instalação e Execução Local

```bash
# 1. Clonar o repositório
git clone https://github.com/gabriel-221/genia_site.git
cd genia_site

# 2. Instalar dependências
npm install

# 3. Configurar variáveis de ambiente
cp .env.local.example .env.local
# Edite .env.local com suas credenciais Supabase e Anthropic

# 4. Iniciar o servidor de desenvolvimento
npm run dev
```

Acesse [http://localhost:3000](http://localhost:3000).

### Outros comandos

```bash
npm run build    # Build de produção (inclui verificação de tipos e ESLint)
npm run start    # Inicia servidor de produção (após build)
npm run lint     # Executa ESLint
```

---

## Estrutura do Projeto

```
genia_site/
├── app/
│   ├── (app)/                    # Grupo de rotas autenticadas
│   │   ├── animais/              # Listagem e cadastro de animais
│   │   │   ├── [id]/page.tsx     # Detalhe do animal
│   │   │   └── novo/page.tsx     # Wizard de cadastro
│   │   ├── calendario/page.tsx   # Calendário de eventos reprodutivos
│   │   ├── copilot/page.tsx      # GENIA Copilot (chat LLM)
│   │   ├── dashboard/page.tsx    # Painel com KPIs do rebanho
│   │   ├── mais/page.tsx         # Perfil, relatórios e configurações
│   │   ├── match/page.tsx        # GeneMatch — ranqueamento e rede
│   │   ├── prenhez/page.tsx      # Simulador de prenhez (IA)
│   │   ├── relatorios/page.tsx   # Relatórios e exportação CSV
│   │   └── layout.tsx            # Bottom navigation compartilhado
│   ├── (auth)/
│   │   └── login/page.tsx        # Autenticação Supabase
│   ├── a/[nfcCode]/page.tsx      # Página pública de rastreabilidade NFC
│   ├── api/
│   │   ├── copilot/route.ts      # API → Anthropic Claude (autenticada, rate-limited)
│   │   └── prenhez/route.ts      # API → inferência Random Forest (com validação)
│   └── layout.tsx                # Root layout + VLibras
├── components/                   # Componentes reutilizáveis
├── lib/
│   ├── genetic-ranking.ts        # Algoritmo GeneMatch
│   ├── supabase/
│   │   ├── client.ts             # Cliente Supabase (cookie storage — SSR compatível)
│   │   └── server.ts             # Cliente Supabase (server-side)
│   └── utils.ts
├── middleware.ts                 # Proteção de rotas autenticadas (cookie-based)
├── supabase/
│   ├── schema.sql                # Schema completo do banco de dados
│   ├── seed_test.sql             # Dados de demonstração (animais NFC públicos)
│   └── seed_ovinos_caprinos.sql  # Seed adicional com ovinos e caprinos
├── types/index.ts                # Tipos TypeScript globais
├── database.md                  # Referência detalhada das colunas do banco
├── next.config.mjs
├── tailwind.config.ts
└── .env.local.example            # Template de variáveis de ambiente
```

---

## Banco de Dados — Referência

O esquema completo está em [`supabase/schema.sql`](supabase/schema.sql). A referência detalhada de colunas está em [`database.md`](database.md).

### Diagrama de Relacionamentos (resumido)

```
auth.users (Supabase Auth)
    │
    └── genia_produtor (1)
             │
             ├── genia_animal (N)
             │       │
             │       ├── genia_pedigree       (relação pai/mãe)
             │       ├── genia_evento_reprodutivo (cio/inseminação/parto...)
             │       ├── genia_ciclo_cio
             │       └── genia_gene_match     (match entre dois animais)
             │
             └── genia_semen (N)
```

### Row Level Security (RLS)

Todas as tabelas têm RLS habilitado. As políticas garantem:
- `genia_produtor`: acesso exclusivo ao próprio perfil (`user_id = auth.uid()`)
- `genia_animal`: escrita apenas para o dono; leitura pública para animais com `disponivel_match = true`
- Demais tabelas: acesso restrito ao dono do animal relacionado

---

## Deploy em Produção

### Vercel (recomendado)

1. Faça fork ou conecte este repositório no [Vercel Dashboard](https://vercel.com/new)
2. Configure as variáveis de ambiente no painel **Settings → Environment Variables**:
   - `NEXT_PUBLIC_SUPABASE_URL`
   - `NEXT_PUBLIC_SUPABASE_ANON_KEY`
   - `ANTHROPIC_API_KEY`
3. O deploy acontece automaticamente a cada push na branch `master`

### Outras plataformas

O projeto é um Next.js padrão e pode ser implantado em qualquer plataforma compatível (Railway, Render, AWS, GCP, etc.):

```bash
npm run build
npm run start
```

---

## Licença

Este software é distribuído sob a **GNU General Public License v3.0**.

Você tem o direito de usar, estudar, modificar e redistribuir este software, desde que as obras derivadas também sejam distribuídas sob os mesmos termos.

Ver o arquivo [LICENSE](LICENSE) para o texto completo.

---

## Equipe

Desenvolvido para o **Hackathon Expoagro Crateús 2026** — Município de Crateús, CE.

**Contato:** projetoarce@gmail.com
