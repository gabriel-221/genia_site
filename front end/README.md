# Front End

Aplicacao Next.js para:

- cadastrar animais e lotes reprodutivos
- estimar probabilidade de prenhez para um cruzamento escolhido usando o modelo treinado
- ranquear potencial genetico por objetivo produtivo

## Estrutura da interface

- `/cadastro`: cadastro dos animais
- `/previsao-cruzamento`: escolha de dois animais e consulta da chance de prenhez do par
- `/painel-genetico`: ranking genetico por objetivo

## Regra importante

O painel genetico nao usa `prenhez estimada` de cruzamento.

A logica foi separada assim:

- `previsao-cruzamento`: usa o modelo de prenhez para um par escolhido pelo usuario
- `painel-genetico`: usa um escore heuristico do proprio animal para leite, corte ou fertilidade

Isso evita tratar a chance de prenhez como se fosse um atributo fixo do animal sem pareamento definido.

## Como rodar

```powershell
cd "front end"
npm install
npm run dev
```

Abra `http://localhost:3000`.

## Observacao

A rota `POST /api/predict` tenta usar o modelo salvo em `../models/random_forest_prenhez.joblib`.
Se a execucao Python falhar, a tela usa um fallback baseado na formula sintetica descrita no README do projeto.
