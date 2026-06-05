# G.E.N.I.A — App Android (Gestão Genética Inteligente de Rebanhos)

> **Branch `genia-apk`** — Hackathon Expoagro Crateús 2026
> Desafio: *Sistema de Coleta de Dados Genéticos para Monitoramento de Inseminação Artificial em Bovinos, Ovinos e Caprinos*

[![Licença: GPL v3](https://img.shields.io/badge/Licen%C3%A7a-GPL%20v3-blue.svg)](../../blob/master/LICENSE)
[![Android](https://img.shields.io/badge/Android-API%2026+-green?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple?logo=kotlin)](https://kotlinlang.org)
[![ONNX](https://img.shields.io/badge/ONNX-embarcado-gray?logo=onnx)](https://onnxruntime.ai)

Aplicativo Android nativo para gestão genética e reprodutiva de rebanhos bovinos, ovinos e caprinos. Funciona **offline-first** com modelo de IA embarcado, leitura de tags NFC e sincronização com Supabase.

---

## Navegação entre branches

| Branch | Componente | Descrição |
|--------|-----------|-----------|
| [`master`](../../tree/master) | **Web PWA** (Next.js) | Interface web, GENIA Copilot, GeneMatch |
| [`genia-apk`](../../tree/genia-apk) | **App Android** (Kotlin) | **← você está aqui** |
| [`data-and-ai`](../../tree/data-and-ai) | **IA & Dados** (Python) | Pipeline de dados, Random Forest, modelos ONNX |

---

## Sumário

- [Atendimento ao Edital](#atendimento-ao-edital)
- [Tecnologias e Frameworks](#tecnologias-e-frameworks)
- [Inteligência Artificial — Implementação](#inteligência-artificial--implementação)
- [Conformidade com a LGPD](#conformidade-com-a-lgpd)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do Banco de Dados](#configuração-do-banco-de-dados-supabase)
- [Configuração do Projeto Android](#configuração-do-projeto-android)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Build e Instalação](#build-e-instalação)
- [Arquitetura](#arquitetura)
- [Estrutura de Pastas](#estrutura-de-pastas)
- [Licença](#licença)

---

## Atendimento ao Edital

| Requisito do Edital | Como o GENIA Android atende | Status |
|---|---|---|
| Cadastro e gestão de animais com dados genéticos por espécie | Wizard de cadastro completo: espécie, raça validada por espécie, pedigree com RFID, escores corporais e histórico reprodutivo | ✅ |
| Registro e monitoramento de ciclos reprodutivos e eventos de IA por espécie | Módulo de Eventos com tipos Cio, Inseminação, Diagnóstico, Parto e Desmame; Calendário visual por animal | ✅ |
| Análise preditiva por IA para estimar taxas de prenhez | Modelo Random Forest treinado e exportado em ONNX — roda embarcado no dispositivo, sem internet | ✅ |
| Geração de relatórios de desempenho genético e reprodutivo | Tela de Relatórios com métricas consolidadas e por espécie (taxa de prenhez, IEP, taxa IA, ganho genético) | ✅ |
| Recomendações por IA para seleção de reprodutores | GeneMatch: algoritmo proprietário que ranqueia o plantel por objetivo genético (Leite, Corte ou Fertilidade) com fatores explicativos | ✅ |
| Interface acessível para técnicos e produtores rurais, inclusive mobile | App Android nativo com Material Design 3, fluxos simplificados e leitura de tag RFID via NFC | ✅ |
| Armazenamento seguro com exportação e integração com rastreabilidade | Banco local Room (offline) + Supabase Postgres com RLS por produtor; IDs UUID compatíveis com rastreabilidade | ✅ |
| IA para predição de resultados de inseminação | Modelo ONNX com 15 variáveis: espécie, raça, idade, peso, ECC, partos, abortos, dias pós-parto, qualidade seminal e parentesco | ✅ |
| Identificação de padrões de fertilidade e desempenho | Dashboard com alertas de cio, diagnóstico pendente e risco genético; histórico visual por animal | ✅ |
| Recomendação automatizada de fêmeas e machos por critérios genéticos | GENIA Copilot: assistente conversacional (Claude Haiku) com acesso ao rebanho real do produtor | ✅ |
| Software livre licenciado sob GNU GPL v3.0 | Repositório público com licença GPL v3.0 | ✅ |

---

## Tecnologias e Frameworks

| Tecnologia | Versão | Finalidade |
|-----------|--------|-----------|
| [Kotlin](https://kotlinlang.org) | 1.9.23 | Linguagem principal — null safety, coroutines, DSLs |
| [Jetpack Compose](https://developer.android.com/compose) | BOM 2024.x | UI declarativa, Material Design 3 |
| [Room](https://developer.android.com/training/data-storage/room) | 6.x | Banco local SQLite com DAOs tipados — armazenamento offline |
| [Supabase Kotlin SDK](https://supabase.com/docs/reference/kotlin) | — | PostgreSQL gerenciado, Auth JWT, Row Level Security |
| [ONNX Runtime for Android](https://onnxruntime.ai) | 1.18 | Inferência do modelo Random Forest diretamente no dispositivo |
| [OkHttp](https://square.github.io/okhttp) | 4.x | Cliente HTTP para API Claude e Supabase REST |
| [Navigation Compose](https://developer.android.com/guide/navigation/navigation-compose) | — | Roteamento entre telas com back stack gerenciado |
| [NFC (Android APIs)](https://developer.android.com/guide/topics/connectivity/nfc) | — | Leitura de tags RFID no campo |
| [Anthropic Claude Haiku](https://www.anthropic.com) | haiku-4-5 | GENIA Copilot — respostas em linguagem natural sobre o rebanho |
| [Gradle](https://gradle.org) | 8.x | Build system com Kotlin DSL |

---

## Inteligência Artificial — Implementação

O app Android incorpora **duas camadas de IA**:

### 1. Predição de Prenhez — ONNX Runtime Embarcado

O modelo `random_forest_prenhez.onnx` (treinado na branch `data-and-ai`) é distribuído dentro do APK. A inferência acontece **100% no dispositivo**, sem necessidade de conexão com internet — fundamental para produtores rurais com conectividade limitada no Sertão.

**Fluxo de execução (`data/ml/PrenhezModelHelper.kt`):**

```
Usuário seleciona matriz + macho
         │
         ▼
AnimalRepository.buildPrenhezInput()
  → monta dict com 15 variáveis do par selecionado
         │
         ▼
PrenhezModelHelper.predict(input)
  → OrtSession.run(inputs)       ← ONNX Runtime, sem internet
  → retorna probability (0.0–1.0)
         │
         ▼
UI exibe percentual + classificação visual
```

**15 variáveis de entrada:** espécie, raças, idade, peso, ECC, partos, abortos, dias pós-parto, filhos nascidos, qualidade seminal e coeficiente de endogamia.

### 2. GeneMatch — Algoritmo de Ranqueamento Genético

Implementado em `data/ml/GeneticRankingHelper.kt`. Pontua cada animal por fatores ponderados segundo o objetivo produtivo selecionado (Leite, Corte ou Fertilidade), com bônus por raça e penalidades por endogamia e abortos.

### 3. GENIA Copilot — LLM (Claude Haiku)

Implementado em `data/remote/AiCopilotRepository.kt`. O assistente:
- Recebe a pergunta do produtor em linguagem natural
- Anexa o contexto real do rebanho (resumo de espécies, prenhas, GeneMatch)
- Envia para a API Anthropic Claude Haiku
- Retorna recomendação em português, sem jargão técnico

A chave da API Anthropic nunca está no código — é lida de `BuildConfig.CLAUDE_API_KEY`, injetada via `local.properties` no build.

---

## Conformidade com a LGPD

| Princípio LGPD | Implementação no App Android |
|----------------|------------------------------|
| **Finalidade** | Dados coletados exclusivamente para gestão do próprio rebanho do produtor autenticado |
| **Necessidade** | Apenas campos estritamente necessários para gestão genética/reprodutiva são coletados e armazenados |
| **Segurança** | Row Level Security (RLS) no Supabase — `auth.uid()` garante isolamento total entre produtores. Credenciais armazenadas em `local.properties` (fora do repositório). Nenhuma chave de API no código-fonte |
| **Autenticidade** | Autenticação via Supabase Auth (JWT). Fallback offline com hash SHA-256 da senha para uso local sem internet |
| **Titularidade** | O produtor pode editar e excluir seus dados a qualquer momento pelo app |
| **Transparência** | Código-fonte aberto (GPL v3.0). O app não coleta dados de uso ou analytics de terceiros |
| **Não discriminação** | Dados não são compartilhados com terceiros nem utilizados para fins comerciais |

**Armazenamento remoto:** Supabase na região `sa-east-1` (São Paulo, Brasil) — soberania de dados em território nacional.

**Armazenamento local:** Room/SQLite no próprio dispositivo do produtor — acesso físico protegido pelo sistema operacional Android.

---

## Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Android Studio | Hedgehog (2023.1.1) ou superior |
| JDK | 17 |
| Android SDK | API 26 (Android 8.0+) |
| Kotlin | 1.9.23 |
| Conta Supabase | Gratuita (Free Tier) |
| Chave API Anthropic | [console.anthropic.com](https://console.anthropic.com) |

---

## Configuração do Banco de Dados (Supabase)

### 1. Criar projeto no Supabase

1. Acesse [supabase.com](https://supabase.com) e crie uma conta
2. Crie um novo projeto — anote a **Project URL** e a **Anon Public Key**
3. Em **Authentication → Settings**, desative **Enable email confirmations** (para desenvolvimento)

### 2. Criar o schema

No **Dashboard → SQL Editor → New Query**, cole e execute `supabase_schema.sql` (raiz deste repositório). Ele cria todas as tabelas, enums, índices e políticas RLS do zero.

> Para bancos já existentes: use `supabase_migrations.sql` — adiciona apenas as colunas novas, sem recriar nada.

### 3. Tabelas criadas

| Tabela | Descrição |
|---|---|
| `genia_produtor` | Perfil do produtor, vinculado ao Supabase Auth |
| `genia_animal` | Rebanho completo com atributos genéticos e reprodutivos |
| `genia_pedigree` | Relações de parentesco pai/mãe entre animais |
| `genia_semen` | Estoque de sêmen por produtor |
| `genia_ciclo_cio` | Ciclos de cio detectados com previsão do próximo |
| `genia_evento_reprodutivo` | Inseminações, partos, diagnósticos, desmames (com score IA) |
| `genia_gene_match` | Histórico de compatibilidade genética entre pares de animais |
| `genia_genetico_alerta` | Alertas de risco de endogamia |

---

## Configuração do Projeto Android

```bash
# Clone do repositório principal (branch genia-apk)
git clone https://github.com/gabriel-221/genia_site.git
cd genia_site
git checkout genia-apk

# Configurar variáveis locais
cp local.properties.example local.properties
# Edite local.properties com seus valores (ver seção abaixo)
```

Abra o projeto no Android Studio, aguarde o Gradle sincronizar e conecte um dispositivo Android (API 26+) ou emulador.

---

## Variáveis de Ambiente

O arquivo `local.properties` **nunca é commitado** (listado no `.gitignore`). O Gradle o lê e expõe as variáveis via `BuildConfig`.

```properties
# Caminho do Android SDK (preenchido automaticamente pelo Android Studio)
sdk.dir=/caminho/para/android/sdk

# Supabase — Dashboard → Project Settings → API
SUPABASE_URL=https://SEU_PROJETO.supabase.co
SUPABASE_ANON_KEY=sua_anon_key_aqui

# Anthropic Claude — console.anthropic.com → API Keys
CLAUDE_API_KEY=sk-ant-api03-SUA_CHAVE_AQUI

# Assinatura release (necessário apenas para build de release)
KEYSTORE_PATH=app/genia.keystore
KEYSTORE_PASSWORD=sua_senha_keystore
KEY_ALIAS=genia
KEY_PASSWORD=sua_senha_key
```

**Gerar keystore de assinatura (uma única vez):**

```bash
keytool -genkey -v -keystore app/genia.keystore \
  -alias genia -keyalg RSA -keysize 2048 -validity 10000
```

---

## Build e Instalação

```bash
# APK debug (desenvolvimento)
./gradlew assembleDebug

# APK release assinado (distribuição)
./gradlew assembleRelease

# Instalar diretamente no dispositivo via USB
adb install app/build/outputs/apk/release/app-release.apk
```

APKs gerados em `app/build/outputs/apk/`.

---

## Arquitetura

O app segue uma arquitetura em camadas com padrão Repository:

```
UI Layer (Jetpack Compose Screens + ViewModels)
                    │
            Repository Layer
           /                  \
    Room Database          SupabaseRepository
    (offline-first)        (sincronização remota)
           │                       │
    SQLite local             PostgreSQL + Auth
    (sempre disponível)      (quando há internet)
```

**Estratégia offline-first:**
- Escritas acontecem imediatamente no Room local
- Sincronização com Supabase ocorre em background ao conectar
- Autenticação: JWT Supabase online + fallback SHA-256 offline

**IA embarcada:**
- `PrenhezModelHelper`: carrega o `.onnx` via `OrtSession` na inicialização
- Inferência síncrona no dispositivo — latência < 50ms, sem internet

---

## Estrutura de Pastas

```
app/src/main/java/com/genoboi/
├── data/
│   ├── local/              # Room v6: AppDatabase, DAOs, Entities, Mappers
│   ├── ml/                 # PrenhezModelHelper (ONNX) + GeneticRankingHelper
│   ├── remote/             # SupabaseConfig, SupabaseRepository, AuthRepository
│   │                       # AiCopilotRepository (Claude Haiku)
│   └── repository/         # AnimalRepository (orquestra local + remoto)
├── domain/model/           # Modelos de domínio (Animal, Produtor, EventoReprodutivo...)
├── ui/
│   ├── animais/            # Lista, detalhe, simulação de prenhez
│   ├── auth/               # Login e cadastro de produtor
│   ├── cadastro/           # Wizard de cadastro em 3 passos
│   ├── calendario/         # Calendário de eventos reprodutivos
│   ├── components/         # Componentes Compose compartilhados + BottomBar
│   ├── copilot/            # GENIA Copilot (chat com IA)
│   ├── dashboard/          # Dashboard KPIs + Relatórios
│   ├── match/              # GeneMatch (ranqueamento + rede de produtores)
│   ├── navigation/         # NavGraph e definição de rotas
│   └── theme/              # Cores, tipografia e tema Material Design 3
└── MainActivity.kt
app/src/main/assets/
└── random_forest_prenhez.onnx   # Modelo ONNX embarcado
```

---

## Licença

Distribuído sob a **GNU General Public License v3.0** — conforme exigido pelo edital do Hackathon Expoagro Crateús 2026.

Ver o arquivo [LICENSE](../../blob/master/LICENSE) para o texto completo.

---

**Contato:** projetoarce@gmail.com
