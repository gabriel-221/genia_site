"use client";

import { useMemo, useState } from "react";
import { useAnimalStore } from "@/lib/animal-store";
import { goalLabels } from "@/lib/config";
import { rankAnimals } from "@/lib/scoring";
import { GeneticGoal } from "@/lib/types";

export default function PainelGeneticoPage() {
  const { animals, isHydrated } = useAnimalStore();
  const [selectedGoal, setSelectedGoal] = useState<GeneticGoal>("fertilidade");

  const rankedAnimals = useMemo(
    () => rankAnimals(animals, selectedGoal),
    [animals, selectedGoal],
  );

  const bestAnimal = rankedAnimals[0];

  return (
    <main className="page-shell">
      <section className="hero">
        <div className="hero-copy">
          <h1>Painel genetico por objetivo</h1>
          <p>
            Escolha a caracteristica de interesse do produtor e veja quais animais do lote
            apresentam melhor alinhamento com esse foco.
          </p>
        </div>

        <div className="hero-grid">
          <article className="metric-card spotlight">
            <span>Objetivo atual</span>
            <strong>{goalLabels[selectedGoal]}</strong>
            <p>O ranking abaixo se reorganiza automaticamente conforme o objetivo selecionado.</p>
          </article>

          <article className="metric-card">
            <span>Melhor animal do objetivo</span>
            <strong>{bestAnimal?.animal.nome ?? "Sem dados"}</strong>
            <p>
              {bestAnimal
                ? `${bestAnimal.label} com escore ${bestAnimal.score.toFixed(1)}.`
                : "Cadastre animais para gerar recomendacoes."}
            </p>
          </article>

          <article className="metric-card">
            <span>Animais analisados</span>
            <strong>{isHydrated ? animals.length : "..."}</strong>
            <p>Todos os animais cadastrados entram no comparativo genetico do painel.</p>
          </article>
        </div>
      </section>

      <section className="panel ranking-panel">
        <div className="panel-heading split">
          <div>
            <h2>Ranking genetico</h2>
            <p>Compare o potencial do plantel para leite, corte ou fertilidade sem usar previsao de cruzamento.</p>
          </div>

          <label className="goal-select">
            Objetivo do produtor
            <select
              value={selectedGoal}
              onChange={(event) => setSelectedGoal(event.target.value as GeneticGoal)}
            >
              <option value="leite">Producao de leite</option>
              <option value="corte">Peso para corte</option>
              <option value="fertilidade">Fertilidade</option>
            </select>
          </label>
        </div>

        <div className="ranking-table">
          <div className="ranking-row ranking-head">
            <span>Animal</span>
            <span>Combinacao genetica</span>
            <span>Base reprodutiva</span>
            <span>Escore</span>
            <span>Leitura tecnica</span>
          </div>

          {rankedAnimals.map((item) => (
            <div className="ranking-row" key={`${selectedGoal}-${item.animal.id}`}>
              <span>{item.animal.nome}</span>
              <span>
                {item.animal.raca_matriz} x {item.animal.raca_macho}
              </span>
              <span>{(item.reproductiveScore * 100).toFixed(1)}</span>
              <span>{item.score.toFixed(1)}</span>
              <span>{item.label}</span>
            </div>
          ))}
        </div>
      </section>
    </main>
  );
}
