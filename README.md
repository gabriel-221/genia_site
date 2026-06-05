# G.E.N.I.A — Pipeline de Dados e Inteligência Artificial

> **Branch `data-and-ai`** — Hackathon Expoagro Crateús 2026

[![Licença: GPL v3](https://img.shields.io/badge/Licen%C3%A7a-GPL%20v3-blue.svg)](../../blob/master/LICENSE)
[![Python](https://img.shields.io/badge/Python-3.11+-blue?logo=python)](https://python.org)
[![scikit-learn](https://img.shields.io/badge/scikit--learn-1.5-orange?logo=scikit-learn)](https://scikit-learn.org)
[![ONNX](https://img.shields.io/badge/ONNX-Runtime-gray?logo=onnx)](https://onnxruntime.ai)

Esta branch contém o **pipeline completo de dados e IA** do GENIA: geração de dataset sintético zootécnico, análise exploratória, treinamento do modelo Random Forest, exportação para ONNX e relatórios de métricas.

O modelo `.onnx` produzido aqui é consumido diretamente pelo **App Android** (ONNX Runtime embarcado) e sua lógica é reimplementada em TypeScript para a **Web PWA** (`/api/prenhez`).

---

## Navegação entre branches

| Branch | Componente | Descrição |
|--------|-----------|-----------|
| [`master`](../../tree/master) | **Web PWA** (Next.js) | Interface web, GENIA Copilot, GeneMatch |
| [`genia-apk`](../../tree/genia-apk) | **App Android** (Kotlin) | Aplicativo nativo com NFC e inferência embarcada |
| [`data-and-ai`](../../tree/data-and-ai) | **IA & Dados** (Python) | **← você está aqui** |

---

## Sumário

- [Tecnologias e Frameworks](#tecnologias-e-frameworks)
- [Inteligência Artificial — Implementação](#inteligência-artificial--implementação)
- [Conformidade com a LGPD](#conformidade-com-a-lgpd)
- [Estrutura de Diretórios](#estrutura-de-diretórios)
- [Configuração do Ambiente](#configuração-do-ambiente)
- [Execução do Pipeline](#execução-do-pipeline)
- [Dataset Sintético](#dataset-sintético)
- [Modelo Random Forest](#modelo-random-forest)
- [Inferência com ONNX](#inferência-com-onnx)
- [Frontend de Demonstração](#frontend-de-demonstração)

---

## Tecnologias e Frameworks

| Tecnologia | Versão | Finalidade |
|-----------|--------|-----------|
| [Python](https://python.org) | ≥ 3.11 | Linguagem principal do pipeline |
| [pandas](https://pandas.pydata.org) | ≥ 2.2 | Manipulação e análise de dados tabulares |
| [NumPy](https://numpy.org) | ≥ 1.26 | Operações numéricas vetorizadas |
| [scikit-learn](https://scikit-learn.org) | ≥ 1.5 | `RandomForestClassifier`, `OneHotEncoder`, `Pipeline`, métricas |
| [skl2onnx](https://onnx.ai/sklearn-onnx) | ≥ 1.18 | Exportação do pipeline sklearn para formato ONNX |
| [ONNX Runtime](https://onnxruntime.ai) | ≥ 1.18 | Inferência do modelo exportado (Python, Android e Web) |
| [matplotlib](https://matplotlib.org) | ≥ 3.9 | Visualizações e gráficos exploratórios |
| [seaborn](https://seaborn.pydata.org) | ≥ 0.13 | Gráficos estatísticos de distribuição |
| [Jupyter](https://jupyter.org) | ≥ 1.0 | Exploração interativa (notebooks opcionais) |

---

## Inteligência Artificial — Implementação

### Problema

Dado um par matriz (fêmea) + reprodutor (macho) com atributos genéticos e reprodutivos, **qual é a probabilidade de prenhez bem-sucedida?**

### Abordagem: Random Forest Classifier

O **Random Forest** foi escolhido como algoritmo baseline por:
- Lidar nativamente com dados tabulares mistos (numérico + categórico após encoding)
- Alta interpretabilidade via importância de variáveis (`feature_importances_`)
- Robustez a ruído e outliers em datasets sintéticos
- Boa generalização sem overfitting com poucos hiperparâmetros
- Compatibilidade com exportação ONNX via `skl2onnx`
- Viabilidade de execução embarcada (Android, sem GPU)

### Pipeline completo

```
src/generate_dataset.py
    │  Gera 10.000 amostras sintéticas com distribuições
    │  zootécnicas realistas (bovinos, ovinos, caprinos)
    ▼
data/raw/dataset.csv
    │
    ▼
src/eda.py
    │  Análise exploratória: distribuições, correlações,
    │  balanceamento de classes, importância preliminar
    ▼
reports/eda_summary.txt
reports/target_distribution.csv
    │
    ▼
src/train_random_forest.py
    │  1. Leitura e split treino/teste (80/20, estratificado)
    │  2. Pipeline sklearn: OneHotEncoder → RandomForestClassifier
    │  3. Treinamento e avaliação (accuracy, F1, AUC-ROC)
    │  4. Exportação: skl2onnx → models/random_forest_prenhez.onnx
    │  5. Geração de relatório de métricas e importância de variáveis
    ▼
models/random_forest_prenhez.onnx    ← consumido pelo Android e pela Web
reports/model_metrics.txt
reports/feature_importance.csv
```

### 15 Variáveis Preditoras

| # | Variável | Tipo | Justificativa zootécnica |
|---|----------|------|--------------------------|
| 1 | `especie` | string | Bovinos, ovinos e caprinos possuem fisiologias reprodutivas distintas |
| 2 | `raca_matriz` | string | Raças leiteiras vs. corte têm diferentes aptidões reprodutivas |
| 3 | `idade_matriz` | float | Fêmeas muito jovens ou velhas têm fertilidade reduzida |
| 4 | `peso_matriz_kg` | float | Subnutrição compromete o ciclo estral |
| 5 | `ecc_matriz` | float (1–5) | ECC é o principal preditor de fertilidade pós-parto |
| 6 | `numero_partos_matriz` | int | Multíparas têm histórico reprodutivo estabelecido |
| 7 | `abortos_matriz` | int | Histórico de abortos indica problemas de saúde reprodutiva |
| 8 | `dias_desde_ultimo_parto` | int | Anestro pós-parto: período crítico de recuperação uterina |
| 9 | `filhos_nascidos_matriz` | int | Fertilidade histórica comprovada |
| 10 | `raca_macho` | string | Compatibilidade genética e aptidão do reprodutor |
| 11 | `idade_macho` | float | Reprodutores muito jovens ou velhos têm qualidade seminal reduzida |
| 12 | `peso_macho_kg` | float | Condição corporal do reprodutor |
| 13 | `qualidade_semen_macho` | float (1–5) | Motilidade e concentração espermática |
| 14 | `filhos_nascidos_macho` | int | Fertilidade do reprodutor comprovada em campo |
| 15 | `parentesco_endogamia` | float (0–1) | Consanguinidade reduz viabilidade dos gametas |

### Fórmula da Probabilidade Sintética (geração do dataset)

```
z = -1.10
    + 1.20 × s_ecc
    + 0.75 × s_parto
    + 0.55 × s_semen
    + 0.45 × s_idade_f
    + 0.20 × s_idade_m
    + 0.35 × s_filhos_macho
    + 0.20 × s_filhos_femea
    - 0.75 × s_abortos
    - 0.95 × s_endogamia

chance_prenhez = sigmoid(z)
prenhou ~ Bernoulli(chance_prenhez)
```

### Portabilidade do Modelo

O arquivo `models/random_forest_prenhez.onnx` é o **único artefato distribuído** entre os três componentes do GENIA:

- **Android** (`genia-apk`): ONNX Runtime for Android — inferência 100% offline no dispositivo
- **Web PWA** (`master`): lógica reimplementada deterministicamente em TypeScript (`app/api/prenhez/route.ts`) — sem dependência de runtime em servidor serverless
- **Python** (esta branch): inferência via `src/inference.py` com `onnxruntime`

---

## Conformidade com a LGPD

| Princípio LGPD | Implementação nesta branch |
|----------------|---------------------------|
| **Necessidade** | O dataset é **100% sintético** — nenhum dado real de produtor ou animal foi coletado ou armazenado |
| **Finalidade** | Modelos treinados exclusivamente para suporte à decisão zootécnica no contexto do GENIA |
| **Segurança** | Nenhuma chave de API, credencial ou dado pessoal é armazenado no repositório |
| **Transparência** | Todo o pipeline de geração, treinamento e validação é open-source e auditável |
| **Responsabilidade** | Código licenciado sob GPL v3.0, permitindo auditoria pública e reprodução dos resultados |

O modelo não toma decisões autônomas sobre animais ou produtores — é uma **ferramenta de suporte** que apresenta estimativas probabilísticas para auxiliar a tomada de decisão humana especializada.

---

## Estrutura de Diretórios

```
data-and-ai/
├── data/
│   ├── raw/              # Datasets brutos (CSV gerado por generate_dataset.py)
│   └── processed/        # Features intermediárias e dados tratados
├── models/
│   └── random_forest_prenhez.onnx   # Modelo treinado — artefato principal
├── reports/              # Métricas, importância de variáveis, figuras EDA
├── src/
│   ├── generate_dataset.py   # Gerador de dataset sintético (10.000 amostras)
│   ├── eda.py                # Análise exploratória de dados
│   ├── train_random_forest.py # Treinamento + exportação ONNX
│   └── inference.py          # Inferência com ONNX Runtime (Python)
├── front end/            # Protótipo web de demonstração (Next.js)
├── requirements.txt      # Dependências Python
└── README.md
```

---

## Configuração do Ambiente

### Linux / macOS

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

### Windows (PowerShell)

```powershell
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

> **Requisito:** Python ≥ 3.11

---

## Execução do Pipeline

### 1. Gerar o dataset sintético

```bash
python src/generate_dataset.py --rows 10000 --seed 42 --output data/raw/dataset.csv
```

### 2. Análise exploratória

```bash
python src/eda.py --input data/raw/dataset.csv --target prenhou
```

Gera em `reports/`:
- `eda_summary.txt` — estatísticas descritivas completas
- `target_distribution.csv` — balanceamento de classes

### 3. Treinar e exportar o modelo

```bash
python src/train_random_forest.py --input data/raw/dataset.csv --target prenhou --n-jobs 1
```

Gera em `reports/`:
- `model_metrics.txt` — accuracy, F1-score, AUC-ROC, matriz de confusão
- `feature_importance.csv` — importância relativa de cada variável

Gera em `models/`:
- `random_forest_prenhez.onnx` — modelo exportado para uso multiplataforma

### 4. Testar inferência

```python
from src.inference import load_pregnancy_model, predict_pregnancy

session = load_pregnancy_model("models/random_forest_prenhez.onnx")

animal_data = {
    "especie": "Bovino",
    "raca_matriz": "Girolando",
    "idade_matriz": 4.5,
    "peso_matriz_kg": 430.0,
    "ecc_matriz": 3.5,
    "numero_partos_matriz": 2,
    "abortos_matriz": 0,
    "dias_desde_ultimo_parto": 90,
    "filhos_nascidos_matriz": 2,
    "raca_macho": "Nelore",
    "idade_macho": 5.0,
    "peso_macho_kg": 520.0,
    "qualidade_semen_macho": 4.0,
    "filhos_nascidos_macho": 15,
    "parentesco_endogamia": 0.05,
}

result = predict_pregnancy(session, animal_data)
print(f"Probabilidade de prenhez: {result['probability']:.1%}")
```

---

## Dataset Sintético

O dataset não contém dados reais. Cada linha representa um **cruzamento hipotético** entre uma fêmea (matriz) e um macho (reprodutor) com atributos realistas para bovinos, ovinos e caprinos do Nordeste brasileiro.

A variável-alvo `prenhou` (0 ou 1) é amostrada via distribuição de Bernoulli com probabilidade determinada pela fórmula sigmoide descrita acima, calibrada com coeficientes derivados da literatura zootécnica.

---

## Modelo Random Forest

O treinamento utiliza um `sklearn.Pipeline` composto por:

```
Pipeline([
    ('preprocessor', ColumnTransformer([
        ('num', StandardScaler(), numeric_cols),
        ('cat', OneHotEncoder(handle_unknown='ignore'), string_cols),
    ])),
    ('classifier', RandomForestClassifier(
        n_estimators=100,
        max_depth=None,
        random_state=42,
        n_jobs=1,
    )),
])
```

A coluna `chance_prenhez_gerada` é **excluída** das features de entrada para evitar data leakage (ela é a probabilidade latente usada para gerar o alvo, não uma variável observável).

---

## Inferência com ONNX

O módulo `src/inference.py` fornece uma interface Python para usar o modelo exportado:

```python
load_pregnancy_model(model_path)   # → ort.InferenceSession
build_onnx_inputs(animal_data)     # → dict[str, np.ndarray]
predict_pregnancy(session, data)   # → {"probability": float, "predicted_class": int}
```

---

## Frontend de Demonstração

A pasta `front end/` contém um protótipo web Next.js com três páginas:

| Página | Função |
|--------|--------|
| `cadastro` | Registro de animais do plantel |
| `previsao-cruzamento` | Escolha de par matriz+macho → chance de prenhez via `POST /api/predict` |
| `painel-genetico` | Ranking genético por objetivo (Leite / Corte / Fertilidade) |

> Para a versão completa e segura da Web PWA, acesse a branch [`master`](../../tree/master).

---

## Licença

GNU General Public License v3.0 — ver [LICENSE](../../blob/master/LICENSE).
