# GENIA — Gestão Genética Inteligente de Rebanhos

Aplicativo Android para manejo reprodutivo e genético de bovinos, ovinos e caprinos, com IA embarcada para predição de prenhez, ranqueamento genético e assistente conversacional (Copilot).

---

## Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do Banco de Dados](#configuração-do-banco-de-dados-supabase)
- [Configuração do Projeto Android](#configuração-do-projeto-android)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Build e Instalação](#build-e-instalação)
- [Arquitetura](#arquitetura)
- [Estrutura de Pastas](#estrutura-de-pastas)
- [Autenticação](#autenticação)
- [Equipe](#equipe)

---

## Sobre o Projeto

**GENIA** é um aplicativo Android desenvolvido para o hackathon **ARCE 2026**, com foco em modernizar o manejo reprodutivo de pequenos e médios produtores rurais do Nordeste. O app funciona **offline-first** — os dados ficam no dispositivo e sincronizam com o Supabase quando há internet.

---

## Funcionalidades

| Módulo | Descrição |
|---|---|
| **Dashboard** | Resumo do rebanho: total de animais, taxa de prenhez, alertas do dia |
| **Animais** | Cadastro completo com pedigree, produção de leite, qualidade seminal e histórico reprodutivo |
| **Simulação de Prenhez** | Modelo Random Forest (ONNX) embarcado — prediz a chance de prenhez de um par matriz × reprodutor |
| **GeneMatch** | Ranqueia todos os animais do rebanho por objetivo genético (Leite, Corte ou Fertilidade) |
| **Calendário** | Linha do tempo de eventos reprodutivos por animal |
| **GENIA Copilot** | Assistente de IA conversacional (Claude Haiku) com contexto real do rebanho |
| **NFC** | Leitura de tags RFID via NFC para identificação rápida de animais |
| **Offline-first** | Login e dados funcionam sem internet; sync automático ao reconectar |

---

## Tecnologias

- **Kotlin** + **Jetpack Compose** (UI declarativa)
- **Room** (banco local SQLite, versão 6)
- **Supabase** (backend: Auth + Postgres + RLS)
- **ONNX Runtime** (modelo de IA embarcado para prenhez)
- **Claude API** (Anthropic) — Copilot conversacional
- **OkHttp** (requisições HTTP)
- **Navigation Compose** (roteamento entre telas)

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
3. Em **Authentication → Settings**, desative **"Enable email confirmations"** para facilitar os testes

### 2. Criar o schema completo

No **Dashboard → SQL Editor → New Query**, cole e execute o conteúdo de `supabase_schema.sql` (disponível na raiz do repositório). Esse arquivo cria todas as tabelas, índices e políticas de segurança (RLS) do zero.

> Para projetos já existentes use `supabase_migrations.sql` — ele adiciona apenas as colunas novas sem recriar nada.

### 3. Tabelas criadas

| Tabela | Descrição |
|---|---|
| `genia_produtor` | Perfil do produtor rural, vinculado ao usuário Supabase Auth |
| `genia_animal` | Rebanho completo (bovinos, ovinos, caprinos) |
| `genia_pedigree` | Relações de parentesco pai/mãe entre animais |
| `genia_semen` | Estoque de sêmen por produtor |
| `genia_ciclo_cio` | Ciclos de cio detectados por animal |
| `genia_evento_reprodutivo` | Inseminações, partos, diagnósticos, desmames |
| `genia_gene_match` | Histórico de matches genéticos solicitados |
| `genia_genetico_alerta` | Alertas de risco de endogamia entre pares |

---

## Configuração do Projeto Android

### 1. Clone o repositório

```bash
git clone https://github.com/gabriel2senag/Teste_Hackaton.git
cd Teste_Hackaton
git checkout nova
```

### 2. Configure as variáveis de ambiente

```bash
cp local.properties.example local.properties
```

Abra `local.properties` e preencha com suas credenciais reais (veja a próxima seção).

### 3. Abra no Android Studio

- **File → Open** → selecione a pasta raiz do projeto
- Aguarde o Gradle sincronizar (pode demorar na primeira vez)
- Conecte um dispositivo Android (API 26+) ou inicie o emulador

---

## Variáveis de Ambiente

O arquivo `local.properties` **nunca é commitado** (listado no `.gitignore`). O Gradle lê esse arquivo durante o build e expõe as variáveis como campos `BuildConfig` no app — sem nenhuma credencial embutida no código-fonte.

```properties
# local.properties — NÃO commitar este arquivo

sdk.dir=/caminho/para/seu/android/sdk

# Supabase — Dashboard -> Project Settings -> API
SUPABASE_URL=https://SEU_PROJETO.supabase.co
SUPABASE_ANON_KEY=sua_anon_key_aqui

# Claude (Anthropic) — https://console.anthropic.com -> API Keys
CLAUDE_API_KEY=sk-ant-api03-...
```

**Onde encontrar cada chave:**

| Variável | Localização |
|---|---|
| `SUPABASE_URL` | Supabase Dashboard → Project Settings → API → **Project URL** |
| `SUPABASE_ANON_KEY` | Supabase Dashboard → Project Settings → API → **anon public** |
| `CLAUDE_API_KEY` | console.anthropic.com → API Keys → **Create Key** |

---

## Build e Instalação

### Instalar direto no dispositivo (debug)

```bash
./gradlew installDebug
```

### Gerar APK debug

```bash
./gradlew assembleDebug
# APK gerado em: app/build/outputs/apk/debug/app-debug.apk
```

### Gerar APK release

```bash
./gradlew assembleRelease
# Requer keystore configurada
```

### Verificar build sem instalar

```bash
./gradlew assembleDebug --stacktrace
```

---

## Arquitetura

O projeto segue o padrão **MVVM** com camadas bem definidas:

```
UI (Compose Screens)
        |
  AnimalRepository / AiCopilotRepository
       /           \
    Room        SupabaseRepository
  (local)          (remoto)
```

- **Offline-first**: toda escrita vai primeiro ao Room, depois sincroniza com Supabase em background via coroutine
- **Auth**: login via Supabase Auth com fallback local usando hash SHA-256 da senha armazenado no Room
- **IA embarcada**: modelo ONNX de Random Forest rodando no dispositivo — sem necessidade de internet para predição de prenhez
- **GENIA Copilot**: chamadas à Claude API (Haiku) com contexto dinâmico do rebanho do produtor

---

## Estrutura de Pastas

```
app/src/main/java/com/genoboi/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room v6, migrations 4->5->6
│   │   ├── dao/                    # AnimalDao, ProdutorDao, EventoDao, CicloDao
│   │   └── entity/                 # Entidades Room + Mappers
│   ├── ml/
│   │   ├── PrenhezModelHelper.kt   # ONNX Runtime — predição de prenhez
│   │   └── GeneticRankingHelper.kt # Score genético por objetivo (Leite/Corte/Fertilidade)
│   ├── remote/
│   │   ├── SupabaseConfig.kt       # Cliente Supabase via BuildConfig
│   │   ├── SupabaseRepository.kt   # CRUD remoto de animais e eventos
│   │   ├── AuthRepository.kt       # Login/cadastro online + fallback offline
│   │   ├── AiCopilotRepository.kt  # Chamadas à Claude API
│   │   └── dto/                    # DTOs de serialização Supabase
│   └── repository/
│       └── AnimalRepository.kt     # Orquestra Room + Supabase
├── domain/model/                   # Modelos de domínio (Animal, Evento, Produtor...)
├── ui/
│   ├── animais/                    # Lista, detalhe, simulação de prenhez
│   ├── auth/                       # Login e cadastro de produtor
│   ├── cadastro/                   # Wizard de cadastro de animal (3 passos)
│   ├── calendario/                 # Calendário de eventos reprodutivos
│   ├── components/                 # Componentes compartilhados + BottomBar
│   ├── copilot/                    # GENIA Copilot (chat IA)
│   ├── dashboard/                  # Dashboard + Relatórios
│   ├── match/                      # GeneMatch — ranking genético
│   ├── navigation/                 # NavGraph + rotas (Screen sealed class)
│   └── theme/                      # Cores e tema Material 3
└── MainActivity.kt
```

---

## Autenticação

### Modo Online

1. Produtor cadastra conta → Supabase Auth cria o usuário
2. Perfil do produtor salvo em `genia_produtor` vinculado ao `auth.uid()`
3. RLS garante isolamento total: cada produtor vê apenas seus próprios dados
4. Animais com `disponivel_match = true` ficam visíveis para outros produtores no GeneMatch

### Modo Offline (sem internet)

1. Hash SHA-256 da senha armazenado na tabela local `produtores` (Room)
2. Animais e eventos já sincronizados anteriormente ficam disponíveis
3. Novos cadastros ficam em fila local e sincronizam automaticamente ao reconectar

---


