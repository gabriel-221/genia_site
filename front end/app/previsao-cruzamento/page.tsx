"use client";

import { useMemo, useState } from "react";
import { useAnimalStore } from "@/lib/animal-store";
import { buildCrossingPayload, formatPercent, requestPrediction } from "@/lib/prediction";
import { PredictionResult } from "@/lib/types";

export default function PrevisaoCruzamentoPage() {
  const { animals, isHydrated } = useAnimalStore();
  const [crossingFemaleId, setCrossingFemaleId] = useState("");
  const [crossingMaleId, setCrossingMaleId] = useState("");
  const [crossingResult, setCrossingResult] = useState<PredictionResult | null>(null);
  const [crossingLabel, setCrossingLabel] = useState("");
  const [isCrossingLoading, setIsCrossingLoading] = useState(false);

  const crossingFemale = useMemo(
    () => animals.find((animal) => animal.id === crossingFemaleId) ?? animals[0] ?? null,
    [animals, crossingFemaleId],
  );
  const crossingMale = useMemo(
    () => animals.find((animal) => animal.id === crossingMaleId) ?? animals[1] ?? animals[0] ?? null,
    [animals, crossingMaleId],
  );

  const crossingSpeciesMismatch =
    crossingFemale !== null &&
    crossingMale !== null &&
    crossingFemale.especie !== crossingMale.especie;
  const crossingSameSelection =
    crossingFemale !== null &&
    crossingMale !== null &&
    crossingFemale.id === crossingMale.id;

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
    } finally {
      setIsCrossingLoading(false);
    }
  }

  return (
    <main className="page-shell">
      <section className="hero">
        <div className="hero-copy">
          <h1>Previsao de cruzamento</h1>
          <p>
            Escolha dois animais cadastrados, monte o par reprodutivo e envie a combinacao para o
            modelo de prenhez.
          </p>
        </div>

        <div className="hero-grid">
          <article className="metric-card spotlight">
            <span>Animais prontos para combinar</span>
            <strong>{isHydrated ? animals.length : "..."}</strong>
            <p>O cruzamento usa os animais cadastrados na pagina de cadastro.</p>
          </article>

          <article className="metric-card">
            <span>Matriz selecionada</span>
            <strong>{crossingFemale?.nome ?? "Selecione"}</strong>
            <p>{crossingFemale ? `${crossingFemale.raca_matriz} • ${crossingFemale.especie}` : "Escolha o primeiro animal."}</p>
          </article>

          <article className="metric-card">
            <span>Macho selecionado</span>
            <strong>{crossingMale?.nome ?? "Selecione"}</strong>
            <p>{crossingMale ? `${crossingMale.raca_matriz} • ${crossingMale.especie}` : "Escolha o segundo animal."}</p>
          </article>
        </div>
      </section>

      <section className="panel crossing-panel">
        <div className="panel-heading split">
          <div>
            <h2>Montar cruzamento</h2>
            <p>O primeiro animal entra como matriz e o segundo como macho na inferencia.</p>
          </div>

          <button
            className="primary-button"
            onClick={estimateCrossing}
            type="button"
            disabled={
              isCrossingLoading ||
              !crossingFemale ||
              !crossingMale ||
              crossingSameSelection ||
              crossingSpeciesMismatch ||
              animals.length < 2
            }
          >
            {isCrossingLoading ? "Calculando..." : "Executar cruzamento"}
          </button>
        </div>

        <div className="crossing-grid">
          <label>
            Animal 1 (matriz)
            <select
              value={crossingFemale?.id ?? ""}
              onChange={(event) => setCrossingFemaleId(event.target.value)}
              disabled={animals.length === 0}
            >
              {animals.map((animal) => (
                <option key={`female-${animal.id}`} value={animal.id}>
                  {animal.nome} - {animal.especie}
                </option>
              ))}
            </select>
          </label>

          <label>
            Animal 2 (macho)
            <select
              value={crossingMale?.id ?? ""}
              onChange={(event) => setCrossingMaleId(event.target.value)}
              disabled={animals.length === 0}
            >
              {animals.map((animal) => (
                <option key={`male-${animal.id}`} value={animal.id}>
                  {animal.nome} - {animal.especie}
                </option>
              ))}
            </select>
          </label>
        </div>

        {animals.length < 2 ? (
          <p className="crossing-warning">
            Cadastre pelo menos dois animais para usar a previsao de cruzamento.
          </p>
        ) : null}

        {crossingSameSelection ? (
          <p className="crossing-warning">
            Escolha dois animais diferentes para realizar o cruzamento.
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
                {crossingFemale.raca_matriz} • {crossingFemale.idade_matriz.toFixed(1)} anos •{" "}
                {crossingFemale.peso_matriz_kg} kg
              </p>
            </div>

            <div>
              <span>Macho escolhido</span>
              <strong>{crossingMale.nome}</strong>
              <p>
                {crossingMale.raca_matriz} • {crossingMale.idade_matriz.toFixed(1)} anos •{" "}
                {crossingMale.peso_matriz_kg} kg
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
    </main>
  );
}
