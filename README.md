# Projeto de Dados e IA

Este workspace foi preparado para analise de dados e treino de modelo na branch `data-and-ai`.

O guia conceitual deste repositorio e o arquivo `Relatorio Dataset Prenhez Random Forest.pdf`, que define as variaveis do dataset sintetico, a formula de geracao da probabilidade de prenhez e a justificativa para uso de Random Forest como baseline.

## Estrutura

- `data/raw/`: datasets brutos
- `data/processed/`: arquivos tratados e features intermediarias
- `notebooks/`: exploracao livre
- `reports/`: relatorios, figuras e metricas exportadas
- `src/`: scripts de analise, geracao e treinamento

## Fluxo sugerido

1. Gerar ou colocar o dataset CSV em `data/raw/`.
2. Rodar a analise exploratoria.
3. Treinar e salvar o modelo.
4. Usar o `.joblib` para inferencia.

## Ambiente

```powershell
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

## Dataset sintetico

O repositorio inclui um gerador em `src/generate_dataset.py` para criar um CSV com 10.000 amostras sinteticas em `data/raw/dataset.csv`.

Cada linha representa:

`matriz + macho -> resultado reprodutivo`

A variavel alvo e:

`prenhou`

Onde:

- `1` = ocorreu prenhez
- `0` = nao ocorreu prenhez

### Variaveis utilizadas

As colunas do dataset seguem a estrutura descrita no PDF:

- `especie`
- `raca_matriz`
- `idade_matriz`
- `peso_matriz_kg`
- `ecc_matriz`
- `numero_partos_matriz`
- `abortos_matriz`
- `dias_desde_ultimo_parto`
- `filhos_nascidos_matriz`
- `raca_macho`
- `idade_macho`
- `peso_macho_kg`
- `qualidade_semen_macho`
- `filhos_nascidos_macho`
- `parentesco_endogamia`
- `chance_prenhez_gerada`
- `prenhou`

### Como o dataset e gerado

O processo de geracao segue a logica do relatorio tecnico:

1. O script sorteia a `especie` entre bovino, ovino e caprino.
2. A partir da especie, sorteia racas, faixas de idade, peso, partos, filhos e dias desde o ultimo parto de forma compativel com cada tipo animal.
3. Gera variaveis biologicas e reprodutivas centrais, como `ecc_matriz`, `qualidade_semen_macho`, `abortos_matriz` e `parentesco_endogamia`.
4. Transforma essas informacoes em escores normalizados.
5. Calcula uma variavel latente `z` com base em uma regressao logistica sintetica.
6. Aplica a funcao sigmoide para obter `chance_prenhez_gerada`.
7. Amostra a coluna `prenhou` como resultado binario final.

### Formula da probabilidade sintetica

O gerador implementa a formulacao descrita no PDF:

```text
chance_prenhez = sigmoid(z)

z = (
  -1.10
  + 1.20 * s_ecc
  + 0.75 * s_parto
  + 0.55 * s_semen
  + 0.45 * s_idade_f
  + 0.20 * s_idade_m
  + 0.35 * s_filhos_macho
  + 0.20 * s_filhos_femea
  - 0.75 * s_abortos
  - 0.95 * s_endogamia
)
```

Esses termos refletem a fundamentacao do relatorio:

- `ecc_matriz` tem peso alto por sua forte associacao com fertilidade
- `dias_desde_ultimo_parto` representa a recuperacao pos-parto
- `qualidade_semen_macho` melhora a chance de fecundacao
- `abortos_matriz` penaliza a probabilidade de prenhez
- `parentesco_endogamia` reduz a chance em casos de maior proximidade genetica
- idade e historico reprodutivo ajudam a representar maturidade e desempenho anterior

### Gerando o CSV

```powershell
python src\generate_dataset.py --rows 10000 --seed 42 --output data\raw\dataset.csv
```

## Analise exploratoria

```powershell
python src\eda.py --input data\raw\dataset.csv --target prenhou
```

Esse comando gera:

- sumario tabular em `reports/eda_summary.txt`
- distribuicoes simples em `reports/target_distribution.csv`

## Treinamento

```powershell
python src\train_random_forest.py --input data\raw\dataset.csv --target prenhou --n-jobs 1
```

Esse script:

- separa treino e teste
- aplica `OneHotEncoder` para colunas categoricas
- remove `chance_prenhez_gerada` das features por padrao para evitar vazamento da propria logica de simulacao
- treina um `RandomForestClassifier`
- salva metricas em `reports/model_metrics.txt`
- salva importancias em `reports/feature_importance.csv`
- salva o pipeline em `models/random_forest_prenhez.joblib`

## Por que usar Random Forest

Seguindo o PDF, a `Random Forest` foi escolhida porque:

- lida bem com dados tabulares
- trabalha bem com relacoes nao lineares
- possui boa interpretabilidade
- funciona bem com interacoes complexas
- tolera ruido e dados sinteticos

Neste repositorio, ela tambem e uma escolha pratica porque:

- aceita bem a mistura de variaveis numericas e categoricas apos encoding
- entrega uma baseline forte com pouco ajuste inicial
- permite extrair importancia das variaveis
- se encaixa bem no objetivo de MVP tecnico

## Inferencia

O arquivo salvo em `models/random_forest_prenhez.joblib` pode ser usado diretamente para inferencia. Ele contem o pipeline completo:

- imputacao de faltantes
- `OneHotEncoder`
- `RandomForestClassifier`

Os dados de entrada devem usar as mesmas colunas do treino, exceto `prenhou`.

Por padrao, o treino tambem exclui `chance_prenhez_gerada`, porque essa coluna representa a probabilidade sintetica interna usada para gerar o alvo e nao deve ser tratada como variavel observada de entrada.

## Frontend e regras de negocio

O frontend em `front end/` separa o fluxo em tres paginas:

- `cadastro`: registro dos animais do plantel
- `previsao-cruzamento`: escolha de dois animais para consultar a chance de prenhez do par
- `painel-genetico`: ranking genetico por objetivo produtivo

### Diferenca entre cruzamento e ranking genetico

Existem duas logicas distintas no sistema:

1. `Previsao de cruzamento`

Essa parte usa o modelo de prenhez. O usuario escolhe dois animais, o frontend monta a combinacao `matriz + macho` e envia os dados para a rota `POST /api/predict`, que tenta usar o pipeline salvo em `models/random_forest_prenhez.joblib`.

O resultado dessa etapa e:

- `chance estimada de prenhez`

Essa previsao so faz sentido quando existe um par escolhido para cruzamento.

2. `Painel genetico por objetivo`

Essa parte nao usa a previsao de prenhez do cruzamento. O ranking genetico foi separado para analisar o potencial individual do animal, sem assumir um pareamento especifico.

O painel usa uma heuristica de escore, implementada no frontend, que combina:

- `ECC`
- qualidade do semen
- historico de filhos
- endogamia
- abortos
- adequacao de idade
- peso, conforme o objetivo selecionado
- bonus por raca alinhada ao objetivo produtivo

O painel mostra dois conceitos:

- `base reprodutiva`: escore reprodutivo intrinseco do animal
- `escore genetico`: nota final do objetivo selecionado (`leite`, `corte` ou `fertilidade`)

Em outras palavras:

- `prenhez estimada` pertence ao fluxo de cruzamento
- `ranking genetico` pertence ao fluxo de selecao e priorizacao do plantel

Essa separacao foi adotada para evitar interpretar uma chance de prenhez como se ela fosse uma propriedade fixa do animal isoladamente.

## Observacoes

- O alvo padrao esperado e `prenhou`.
- O parametro `--n-jobs 1` foi adotado no exemplo de treino para evitar falhas de paralelismo no ambiente atual.
- O dataset e sintetico e serve como base de MVP, nao como substituto de base zootecnica real validada em campo.
