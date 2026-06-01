"use client";

import { ChangeEvent, useMemo, useState } from "react";
import { useAnimalStore } from "@/lib/animal-store";
import { baseForm, speciesBreeds } from "@/lib/config";
import { buildId, formatPercent } from "@/lib/prediction";
import { estimatePregnancyLocally } from "@/lib/scoring";
import { AnimalRecord } from "@/lib/types";

export default function CadastroPage() {
  const { animals, setAnimals, isHydrated } = useAnimalStore();
  const [formData, setFormData] = useState<AnimalRecord>(baseForm);

  const averagePregnancyRate = useMemo(() => {
    if (!animals.length) {
      return 0;
    }

    const total = animals.reduce(
      (sum, animal) => sum + estimatePregnancyLocally(animal).probability,
      0,
    );

    return total / animals.length;
  }, [animals]);

  function updateFormField(field: keyof AnimalRecord, value: string | number) {
    setFormData((current) => {
      const nextData = {
        ...current,
        [field]: value,
      };

      if (field === "especie") {
        const nextSpecies = value as AnimalRecord["especie"];
        nextData.raca_matriz = speciesBreeds[nextSpecies].female[0];
        nextData.raca_macho = speciesBreeds[nextSpecies].male[0];
      }

      return nextData;
    });
  }

  function handleFieldChange(event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value, type } = event.target;
    const normalizedValue = type === "number" ? Number(value) : value;
    updateFormField(name as keyof AnimalRecord, normalizedValue);
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const nextAnimal = {
      ...formData,
      id: buildId(formData.nome || "animal"),
    };

    setAnimals((current) => [nextAnimal, ...current]);
    setFormData(baseForm);
  }

  return (
    <main className="page-shell">
      <section className="hero">
        <div className="hero-copy">
          <h1>Cadastro de animais</h1>
          <p>
            Registre as matrizes e reprodutores do plantel para usar depois na previsao de
            cruzamento e no painel genetico por objetivo.
          </p>
        </div>

        <div className="hero-grid">
          <article className="metric-card spotlight">
            <span>Animais disponiveis</span>
            <strong>{isHydrated ? animals.length : "..."}</strong>
            <p>Todos os animais cadastrados aqui ficam disponiveis nas outras paginas.</p>
          </article>

          <article className="metric-card">
            <span>Taxa media local estimada</span>
            <strong>{formatPercent(averagePregnancyRate)}</strong>
            <p>Media calculada pela formula sintetica para leitura rapida do lote atual.</p>
          </article>

          <article className="metric-card">
            <span>Fluxo recomendado</span>
            <strong>Cadastro → Cruzamento → Painel</strong>
            <p>Cadastre primeiro, depois compare pares e por fim avalie o objetivo genetico.</p>
          </article>
        </div>
      </section>

      <section className="content-grid">
        <article className="panel form-panel">
          <div className="panel-heading">
            <div>
              <h2>Novo animal</h2>
              <p>Preencha os dados zootecnicos principais para adicionar o animal ao plantel.</p>
            </div>
          </div>

          <form className="animal-form" onSubmit={handleSubmit}>
            <label>
              Nome do lote ou animal
              <input
                name="nome"
                value={formData.nome}
                onChange={handleFieldChange}
                placeholder="Ex.: Matriz Aurora 12"
                required
              />
            </label>

            <div className="form-grid">
              <label>
                Especie
                <select name="especie" value={formData.especie} onChange={handleFieldChange}>
                  {Object.keys(speciesBreeds).map((species) => (
                    <option key={species} value={species}>
                      {species}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Raca da matriz
                <select
                  name="raca_matriz"
                  value={formData.raca_matriz}
                  onChange={handleFieldChange}
                >
                  {speciesBreeds[formData.especie].female.map((breed) => (
                    <option key={breed} value={breed}>
                      {breed}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Idade da matriz
                <input
                  name="idade_matriz"
                  type="number"
                  step="0.1"
                  value={formData.idade_matriz}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                Peso da matriz (kg)
                <input
                  name="peso_matriz_kg"
                  type="number"
                  value={formData.peso_matriz_kg}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                ECC da matriz
                <input
                  name="ecc_matriz"
                  type="number"
                  step="0.1"
                  min="1"
                  max="5"
                  value={formData.ecc_matriz}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                Numero de partos
                <input
                  name="numero_partos_matriz"
                  type="number"
                  value={formData.numero_partos_matriz}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                Abortos
                <input
                  name="abortos_matriz"
                  type="number"
                  value={formData.abortos_matriz}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                Dias desde o ultimo parto
                <input
                  name="dias_desde_ultimo_parto"
                  type="number"
                  value={formData.dias_desde_ultimo_parto}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                Filhos nascidos da matriz
                <input
                  name="filhos_nascidos_matriz"
                  type="number"
                  value={formData.filhos_nascidos_matriz}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                Raca do macho de referencia
                <select
                  name="raca_macho"
                  value={formData.raca_macho}
                  onChange={handleFieldChange}
                >
                  {speciesBreeds[formData.especie].male.map((breed) => (
                    <option key={breed} value={breed}>
                      {breed}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Idade do macho de referencia
                <input
                  name="idade_macho"
                  type="number"
                  step="0.1"
                  value={formData.idade_macho}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                Peso do macho de referencia (kg)
                <input
                  name="peso_macho_kg"
                  type="number"
                  value={formData.peso_macho_kg}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                Qualidade do semen
                <input
                  name="qualidade_semen_macho"
                  type="number"
                  min="1"
                  max="5"
                  value={formData.qualidade_semen_macho}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                Filhos nascidos do macho
                <input
                  name="filhos_nascidos_macho"
                  type="number"
                  value={formData.filhos_nascidos_macho}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                Parentesco / endogamia
                <input
                  name="parentesco_endogamia"
                  type="number"
                  step="0.01"
                  min="0"
                  max="1"
                  value={formData.parentesco_endogamia}
                  onChange={handleFieldChange}
                />
              </label>
            </div>

            <button className="primary-button" type="submit">
              Adicionar ao plantel
            </button>
          </form>
        </article>

        <article className="panel">
          <div className="panel-heading">
            <div>
              <h2>Plantel cadastrado</h2>
              <p>Resumo dos animais disponiveis para cruzamento e selecao genetica.</p>
            </div>
          </div>

          <div className="animal-list">
            {animals.map((animal) => {
              const preview = estimatePregnancyLocally(animal);
              return (
                <article className="animal-card" key={animal.id}>
                  <div className="animal-card-header">
                    <div>
                      <h3>{animal.nome}</h3>
                      <p>
                        {animal.especie} - {animal.raca_matriz} x {animal.raca_macho}
                      </p>
                    </div>
                    <span className={`prediction-tag ${preview.predictedClass ? "positive" : "neutral"}`}>
                      {preview.predictedClass ? "Bom perfil" : "Atencao"}
                    </span>
                  </div>

                  <div className="animal-metrics">
                    <div>
                      <span>Prenhez local</span>
                      <strong>{formatPercent(preview.probability)}</strong>
                    </div>
                    <div>
                      <span>ECC</span>
                      <strong>{animal.ecc_matriz.toFixed(1)}</strong>
                    </div>
                    <div>
                      <span>Endogamia</span>
                      <strong>{animal.parentesco_endogamia.toFixed(2)}</strong>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        </article>
      </section>
    </main>
  );
}
