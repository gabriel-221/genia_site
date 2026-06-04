# GENIA — Gestão Genética Inteligente de Rebanhos

**Hackathon Expoagro Crateús 2026**
Desafio: *Sistema de Coleta de Dados Genéticos para Monitoramento de Inseminação Artificial em Bovinos, Ovinos e Caprinos*

---

## O Desafio

O edital do Hackathon Expoagro Crateús 2026 propõe o desenvolvimento de uma solução tecnológica com Inteligência Artificial para apoiar a coleta, o registro e a análise de dados genéticos de rebanhos bovinos, ovinos e caprinos, com foco no monitoramento de programas de inseminação artificial.

O GENIA não apenas atende — supera cada um dos requisitos obrigatórios do edital, entregando funcionalidades adicionais que ampliam o impacto real sobre os produtores rurais do Sertão de Crateús.

---

## Atendimento ao Edital — Requisito por Requisito

| Requisito do Edital | Como o GENIA atende | Status |
|---|---|---|
| Cadastro e gestão de animais com dados genéticos por espécie | Wizard de cadastro completo: espécie, raça validada por espécie, pedigree com RFID, escores e histórico reprodutivo | ✅ Completo |
| Registro e monitoramento de ciclos reprodutivos e eventos de IA por espécie | Módulo de Eventos com tipos Cio, Inseminação, Diagnóstico, Parto e Desmame; Calendário visual por animal | ✅ Completo |
| Análise preditiva por IA para estimar taxas de prenhez | Modelo Random Forest treinado e exportado em ONNX — roda embarcado no dispositivo, sem internet | ✅ Completo |
| Geração de relatórios de desempenho genético e reprodutivo | Tela de Relatórios com métricas consolidadas e por espécie (taxa de prenhez, IEP, taxa IA, ganho genético) | ✅ Completo |
| Recomendações por IA para seleção de reprodutores | GeneMatch: algoritmo proprietário que ranqueia o plantel por objetivo genético (Leite, Corte ou Fertilidade) com fatores explicativos | ✅ Completo |
| Interface acessível para técnicos e produtores rurais, inclusive mobile | App Android nativo com Material Design 3, fluxos simplificados e leitura de tag RFID via NFC | ✅ Completo |
| Armazenamento seguro com exportação e integração com rastreabilidade | Banco local Room (offline) + Supabase Postgres com RLS por produtor; IDs UUID compatíveis com rastreabilidade | ✅ Completo |
| IA para predição de resultados de inseminação | Modelo ONNX com 15 variáveis: espécie, raça, idade, peso, ECC, partos, abortos, dias pós-parto, qualidade seminal e parentesco | ✅ Completo |
| Identificação de padrões de fertilidade e desempenho | Dashboard com alertas de cio, diagnóstico pendente e risco genético; histórico visual por animal | ✅ Completo |
| Recomendação automatizada de fêmeas e machos por critérios genéticos | GENIA Copilot: assistente conversacional (Claude Haiku) com acesso ao rebanho real do produtor | ✅ Completo |
| Software livre licenciado sob GNU GPL v3.0 | Repositório público no GitHub com licença GPL v3.0 | ✅ Completo |

---

## Funcionalidades Além do Edital

| Funcionalidade Extra | Descrição |
|---|---|
| **IA embarcada offline** | O modelo de predição de prenhez roda direto no dispositivo via ONNX Runtime — funciona sem internet, essencial para o Sertão |
| **Autenticação offline-first** | Produtor faz login e acessa seus dados mesmo sem conexão; sync automático ao reconectar |
| **GENIA Copilot** | Assistente de IA conversacional (Claude Haiku) com contexto real do rebanho — produtor pergunta em linguagem natural |
| **GeneMatch entre produtores** | Animais marcados como disponíveis ficam visíveis para outros produtores, criando uma rede de melhoramento genético regional |
| **NFC/RFID** | Identificação de animais por aproximação de tag NFC, agilizando o manejo de campo |
| **Produção de leite diária** | Campo específico que influencia o cálculo do ranking genético para o objetivo Leite |
| **Isolamento por produtor com RLS** | Row Level Security no Supabase garante que cada produtor acessa exclusivamente seus dados — conformidade com LGPD |

---

## Critérios de Avaliação — Como o GENIA se posiciona

### Etapa 1 — Online

| Critério | Peso | Argumento |
|---|---|---|
| **Impacto** | 50% | Atende 100% dos requisitos do edital com funcionalidades extras; solução pronta para uso real por produtores rurais |
| **Viabilidade de Implementação** | 20% | Stack consolidada (Android + Supabase + ONNX); app funcional e instalável já na Etapa 1 |
| **Criatividade e Originalidade** | 30% | IA embarcada offline + Copilot conversacional + GeneMatch entre produtores — combinação inédita no contexto regional |

### Etapa 2 — Presencial

| Critério | Peso | Como atendemos |
|---|---|---|
| Originalidade e Inovação | 20% | Copilot com IA generativa + modelo preditivo embarcado + rede de GeneMatch entre produtores |
| Relevância e Impacto para o Produtor Rural | 20% | Interface simplificada, funciona offline, leitura NFC no campo, responde perguntas em português natural |
| Execução e Funcionalidade | 20% | Solução 100% operacional, APK assinado disponível, todos os fluxos funcionando |
| Viabilidade Técnica | 20% | Stack open-source, código aberto GPL v3.0, banco documentado, setup reproduzível |
| Escalabilidade e Sustentabilidade | 10% | Arquitetura multi-produtor com RLS; GeneMatch cria valor de rede regional |
| Segurança de Dados e Privacidade (LGPD) | 10% | RLS por auth.uid(), credenciais fora do código via local.properties, dados sensíveis protegidos |

---

## Tecnologias

- **Kotlin** + **Jetpack Compose** — interface moderna e fluida
- **Room** (SQLite local, v6) — banco offline-first
- **Supabase** (Postgres + Auth + RLS) — backend seguro e escalável
- **ONNX Runtime** — modelo Random Forest de predição de prenhez embarcado no dispositivo
- **Claude Haiku** (Anthropic) — GENIA Copilot conversacional
- **OkHttp** — chamadas HTTP eficientes
- **Navigation Compose** — roteamento entre telas

---

## Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Android Studio | Hedgehog (2023.1.1) ou superior |
| JDK | 17 |
| Android SDK | API 26 (Android 8.0+) |
| Kotlin | 1.9.23 |
| Conta Supabase | Gratuita (Free Tier) |
| Chave API Anthropic | console.anthropic.com |

---

## Configuração do Banco de Dados (Supabase)

### 1. Criar projeto no Supabase

1. Acesse [supabase.com](https://supabase.com) e crie uma conta
2. Crie um novo projeto e anote a **Project URL** e a **anon public key**
3. Em **Authentication → Settings**, desative **Enable email confirmations**

### 2. Criar o schema

No **Dashboard → SQL Editor → New Query**, cole e execute o arquivo `supabase_schema.sql` disponível na raiz do repositório. Ele cria todas as tabelas, índices e políticas RLS do zero.

> Para bancos já existentes, use `supabase_migrations.sql` — adiciona apenas as colunas novas sem recriar nada.

### 3. Tabelas criadas

| Tabela | Descrição |
|---|---|
| `genia_produtor` | Perfil do produtor, vinculado ao Supabase Auth |
| `genia_animal` | Rebanho completo com todos os atributos genéticos |
| `genia_pedigree` | Relações de parentesco pai/mãe |
| `genia_semen` | Estoque de sêmen por produtor |
| `genia_ciclo_cio` | Ciclos de cio detectados |
| `genia_evento_reprodutivo` | Inseminações, partos, diagnósticos, desmames |
| `genia_gene_match` | Histórico de matches genéticos |
| `genia_genetico_alerta` | Alertas de risco de endogamia |

---

## Configuração do Projeto Android

```bash
git clone https://github.com/gabriel2senag/Teste_Hackaton.git
cd Teste_Hackaton
git checkout nova
cp local.properties.example local.properties
# edite local.properties com suas credenciais
```

Abra no Android Studio, aguarde o Gradle sincronizar e conecte um dispositivo Android (API 26+).

---

## Variáveis de Ambiente

O arquivo `local.properties` nunca é commitado (`.gitignore`). O Gradle o lê e expõe as variáveis via `BuildConfig`.

```properties
sdk.dir=/caminho/para/android/sdk

# Supabase — Dashboard -> Project Settings -> API
SUPABASE_URL=https://SEU_PROJETO.supabase.co
SUPABASE_ANON_KEY=sua_anon_key_aqui

# Claude — console.anthropic.com -> API Keys
CLAUDE_API_KEY=sk-ant-api03-...

# Assinatura release
KEYSTORE_PATH=app/genia.keystore
KEYSTORE_PASSWORD=sua_senha
KEY_ALIAS=genia
KEY_PASSWORD=sua_senha
```

**Gerar a keystore de assinatura (uma vez):**

```bash
keytool -genkey -v -keystore app/genia.keystore \
  -alias genia -keyalg RSA -keysize 2048 -validity 10000
```

---

## Build e Instalação

```bash
# APK debug (testes rápidos)
./gradlew assembleDebug

# APK release assinado (sem aviso de app perigoso — 1 confirmacao apenas)
./gradlew assembleRelease

# Instalar direto no dispositivo via USB (zero avisos)
adb install app/build/outputs/apk/release/app-release.apk
```

APKs em `app/build/outputs/apk/`.

---

## Arquitetura

```
UI (Compose Screens)
        |
  AnimalRepository / AiCopilotRepository
       /           \
    Room        SupabaseRepository
  (local)          (remoto)
```

- **Offline-first**: escrita local imediata; sync Supabase em background
- **Auth**: Supabase Auth online + fallback SHA-256 offline
- **IA embarcada**: ONNX Runtime no dispositivo, sem internet
- **Copilot**: Claude Haiku com contexto real do rebanho

---

## Estrutura de Pastas

```
app/src/main/java/com/genoboi/
├── data/
│   ├── local/          # Room v6 (AppDatabase, DAOs, Entities, Mappers)
│   ├── ml/             # PrenhezModelHelper (ONNX) + GeneticRankingHelper
│   ├── remote/         # SupabaseConfig, SupabaseRepository, AuthRepository, AiCopilotRepository
│   └── repository/     # AnimalRepository (orquestra local + remoto)
├── domain/model/       # Modelos de dominio puros
├── ui/
│   ├── animais/        # Lista, detalhe, simulacao de prenhez
│   ├── auth/           # Login e cadastro
│   ├── cadastro/       # Wizard de cadastro (3 passos)
│   ├── calendario/     # Calendario de eventos
│   ├── components/     # Componentes compartilhados + BottomBar
│   ├── copilot/        # GENIA Copilot (chat IA)
│   ├── dashboard/      # Dashboard + Relatorios
│   ├── match/          # GeneMatch
│   ├── navigation/     # NavGraph + rotas
│   └── theme/          # Cores e tema Material 3
└── MainActivity.kt
```

---

## Licenca

Distribuido sob a licenca **GNU GPL v3.0** — conforme exigido pelo edital do Hackathon Expoagro Crateús 2026.
