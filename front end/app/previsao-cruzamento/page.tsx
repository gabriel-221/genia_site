"use client";

import { useEffect, useMemo, useState } from "react";
import { useAnimalStore } from "@/lib/animal-store";
import { buildCrossingPayload, formatPercent, requestPrediction } from "@/lib/prediction";
import { PredictionResult, PregnancyInput } from "@/lib/types";

const examplePayload: PregnancyInput = {
  especie: "Bovino",
  raca_matriz: "Girolando",
  idade_matriz: 4.8,
  peso_matriz_kg: 510,
  ecc_matriz: 3.5,
  numero_partos_matriz: 2,
  abortos_matriz: 0,
  dias_desde_ultimo_parto: 118,
  filhos_nascidos_matriz: 2,
  raca_macho: "Gir",
  idade_macho: 4.7,
  peso_macho_kg: 760,
  qualidade_semen_macho: 5,
  filhos_nascidos_macho: 18,
  parentesco_endogamia: 0.04,
};

const requiredJsonFields: Array<keyof PregnancyInput> = [
  "especie",
  "raca_matriz",
  "idade_matriz",
  "peso_matriz_kg",
  "ecc_matriz",
  "numero_partos_matriz",
  "abortos_matriz",
  "dias_desde_ultimo_parto",
  "filhos_nascidos_matriz",
  "raca_macho",
  "idade_macho",
  "peso_macho_kg",
  "qualidade_semen_macho",
  "filhos_nascidos_macho",
  "parentesco_endogamia",
];

export default function PrevisaoCruzamentoPage() {
  const { animals, isHydrated } = useAnimalStore();
  const [crossingFemaleId, setCrossingFemaleId] = useState("");
  const [crossingMaleId, setCrossingMaleId] = useState("");
  const [crossingResult, setCrossingResult] = useState<PredictionResult | null>(null);
  const [crossingLabel, setCrossingLabel] = useState("");
  const [isCrossingLoading, setIsCrossingLoading] = useState(false);
  const [jsonInput, setJsonInput] = useState("");
  const [jsonError, setJsonError] = useState("");

  const femaleAnimals = useMemo(
    () => animals.filter((animal) => animal.sexo === "femea"),
    [animals],
  );
  const maleAnimals = useMemo(
    () => animals.filter((animal) => animal.sexo === "macho"),
    [animals],
  );

  const crossingFemale = useMemo(
    () => femaleAnimals.find((animal) => animal.id === crossingFemaleId) ?? femaleAnimals[0] ?? null,
    [femaleAnimals, crossingFemaleId],
  );
  const crossingMale = useMemo(
    () => maleAnimals.find((animal) => animal.id === crossingMaleId) ?? maleAnimals[0] ?? null,
    [maleAnimals, crossingMaleId],
  );

  const crossingSpeciesMismatch =
    crossingFemale !== null &&
    crossingMale !== null &&
    crossingFemale.especie !== crossingMale.especie;

  useEffect(() => {
    if (!crossingFemale || !crossingMale || crossingSpeciesMismatch) {
      setJsonInput(JSON.stringify(examplePayload, null, 2));
      return;
    }

    const payload = buildCrossingPayload(crossingFemale, crossingMale);
    setJsonInput(JSON.stringify(payload, null, 2));
  }, [crossingFemale, crossingMale, crossingSpeciesMismatch]);

  function validatePregnancyInput(payload: PregnancyInput) {
    for (const field of requiredJsonFields) {
      if (!(field in payload)) {
        throw new Error(`Campo obrigatorio ausente: ${field}`);
      }
    }
  }

  async function estimateCrossing() {
    if (!crossingFemale || !crossingMale || crossingSpeciesMismatch) {
      return;
    }

    const payload = buildCrossingPayload(crossingFemale, crossingMale);
    setIsCrossingLoading(true);

    try {
      const result = await requestPrediction(payload);
      setCrossingResult(result);
      setCrossingLabel(`${crossingFemale.nome} x ${crossingMale.nome}`);
      setJsonError("");
    } finally {
      setIsCrossingLoading(false);
    }
  }

  async function estimateJsonInput() {
    setJsonError("");
    setIsCrossingLoading(true);

    try {
      const parsedPayload = JSON.parse(jsonInput) as PregnancyInput;
      validatePregnancyInput(parsedPayload);
      const result = await requestPrediction(parsedPayload);
      setCrossingResult(result);
      setCrossingLabel("Inferencia via JSON");
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "O JSON informado nao e valido para inferencia.";
      setJsonError(`${message} Revise a estrutura e tente novamente.`);
    } finally {
      setIsCrossingLoading(false);
    }
  }

  function refreshJsonFromSelectedAnimals() {
    if (!crossingFemale || !crossingMale || crossingSpeciesMismatch) {
      return;
    }

    const payload = buildCrossingPayload(crossingFemale, crossingMale);
    setJsonInput(JSON.stringify(payload, null, 2));
    setJsonError("");
  }

  function loadExampleJson() {
    setJsonInput(JSON.stringify(examplePayload, null, 2));
    setJsonError("");
  }

  return (
    <main className="page-shell">
      <section className="hero">
        <div className="hero-copy">
          <h1>Previsao de cruzamento</h1>
          <p>
            Escolha uma femea para matriz e um macho para reprodutor. O sistema monta a entrada do
            modelo com esse par individual.
          </p>
        </div>

        <div className="hero-grid">
          <article className="metric-card spotlight">
            <span>Femeas disponiveis</span>
            <strong>{isHydrated ? femaleAnimals.length : "..."}</strong>
            <p>Somente femeas aparecem no seletor de matriz.</p>
          </article>

          <article className="metric-card">
            <span>Machos disponiveis</span>
            <strong>{isHydrated ? maleAnimals.length : "..."}</strong>
            <p>Somente machos aparecem no seletor de reprodutor.</p>
          </article>

          <article className="metric-card">
            <span>Regra do cruzamento</span>
            <strong>1 femea + 1 macho</strong>
            <p>O par precisa ser da mesma especie para alimentar o modelo de prenhez.</p>
          </article>
        </div>
      </section>

      <section className="panel crossing-panel">
        <div className="panel-heading split">
          <div>
            <h2>Montar cruzamento</h2>
            <p>Selecione a matriz e o reprodutor para preencher o modelo de inferencia.</p>
          </div>

          <button
            className="primary-button"
            onClick={estimateCrossing}
            type="button"
            disabled={
              isCrossingLoading ||
              !crossingFemale ||
              !crossingMale ||
              crossingSpeciesMismatch ||
              femaleAnimals.length < 1 ||
              maleAnimals.length < 1
            }
          >
            {isCrossingLoading ? "Calculando..." : "Executar cruzamento"}
          </button>
        </div>

        <div className="crossing-grid">
          <label>
            Femea (matriz)
            <select
              value={crossingFemale?.id ?? ""}
              onChange={(event) => setCrossingFemaleId(event.target.value)}
              disabled={femaleAnimals.length === 0}
            >
              {femaleAnimals.map((animal) => (
                <option key={`female-${animal.id}`} value={animal.id}>
                  {animal.nome} - {animal.especie} - {animal.raca}
                </option>
              ))}
            </select>
          </label>

          <label>
            Macho (reprodutor)
            <select
              value={crossingMale?.id ?? ""}
              onChange={(event) => setCrossingMaleId(event.target.value)}
              disabled={maleAnimals.length === 0}
            >
              {maleAnimals.map((animal) => (
                <option key={`male-${animal.id}`} value={animal.id}>
                  {animal.nome} - {animal.especie} - {animal.raca}
                </option>
              ))}
            </select>
          </label>
        </div>

        {femaleAnimals.length < 1 || maleAnimals.length < 1 ? (
          <p className="crossing-warning">
            Cadastre pelo menos uma femea e um macho para usar a previsao de cruzamento.
          </p>
        ) : null}

        {crossingSpeciesMismatch ? (
          <p className="crossing-warning">
            Escolha animais da mesma especie para realizar o cruzamento no modelo.
          </p>
        ) : null}

        {crossingFemale && crossingMale ? (
          <div className="crossing-summary">
            <div>
              <span>Matriz escolhida</span>
              <strong>{crossingFemale.nome}</strong>
              <p>
                {crossingFemale.raca} - {crossingFemale.idade.toFixed(1)} anos - {crossingFemale.peso_kg} kg
              </p>
            </div>

            <div>
              <span>Reprodutor escolhido</span>
              <strong>{crossingMale.nome}</strong>
              <p>
                {crossingMale.raca} - {crossingMale.idade.toFixed(1)} anos - {crossingMale.peso_kg} kg
              </p>
            </div>
          </div>
        ) : null}

        {crossingResult ? (
          <article className="crossing-result">
            <span>Resultado do cruzamento</span>
            <strong>{crossingLabel}</strong>
            <h3>{formatPercent(crossingResult.probability)} de chance estimada de prenhez</h3>
            <p>
              Fonte: {crossingResult.source === "modelo" ? "modelo Random Forest" : "formula sintetica"}.
              {" "}{crossingResult.explanation}
            </p>
          </article>
        ) : null}
      </section>

      <section className="panel crossing-panel">
        <div className="panel-heading split">
          <div>
            <h2>Inferencia por JSON</h2>
            <p>
              Preencha ou cole um JSON no formato do modelo. Voce pode usar o casal selecionado
              acima como ponto de partida.
            </p>
          </div>

          <div className="json-actions">
            <button
              className="ghost-button"
              onClick={loadExampleJson}
              type="button"
            >
              Carregar exemplo
            </button>

            <button
              className="secondary-button"
              onClick={refreshJsonFromSelectedAnimals}
              type="button"
              disabled={!crossingFemale || !crossingMale || crossingSpeciesMismatch}
            >
              Gerar JSON do casal
            </button>

            <button
              className="primary-button"
              onClick={estimateJsonInput}
              type="button"
              disabled={isCrossingLoading || !jsonInput.trim()}
            >
              {isCrossingLoading ? "Inferindo..." : "Inferir JSON"}
            </button>
          </div>
        </div>

        <label className="json-editor-label">
          JSON de inferencia
          <textarea
            className="json-editor"
            value={jsonInput}
            onChange={(event) => setJsonInput(event.target.value)}
            spellCheck={false}
            rows={16}
          />
        </label>

        {jsonError ? <p className="crossing-warning">{jsonError}</p> : null}
      </section>
    </main>
  );
}
