"use client";

import { ChangeEvent, useMemo, useState } from "react";
import { useAnimalStore } from "@/lib/animal-store";
import { baseForm, speciesBreeds } from "@/lib/config";
import { buildId, formatPercent } from "@/lib/prediction";
import { AnimalRecord } from "@/lib/types";
import { scoreAnimalReproductiveBase } from "@/lib/scoring";

export default function CadastroPage() {
  const { animals, setAnimals, isHydrated } = useAnimalStore();
  const [formData, setFormData] = useState<AnimalRecord>(baseForm);

  const femalesCount = animals.filter((animal) => animal.sexo === "femea").length;
  const malesCount = animals.filter((animal) => animal.sexo === "macho").length;

  const averageReproductiveRate = useMemo(() => {
    if (!animals.length) {
      return 0;
    }

    const total = animals.reduce(
      (sum, animal) => sum + scoreAnimalReproductiveBase(animal),
      0,
    );

    return total / animals.length;
  }, [animals]);

  function updateFormField(field: keyof AnimalRecord, value: string | number | null) {
    setFormData((current) => {
      const nextData = {
        ...current,
        [field]: value,
      };

      if (field === "especie") {
        const nextSpecies = value as AnimalRecord["especie"];
        nextData.raca =
          current.sexo === "femea"
            ? speciesBreeds[nextSpecies].female[0]
            : speciesBreeds[nextSpecies].male[0];
      }

      if (field === "sexo") {
        const nextSex = value as AnimalRecord["sexo"];
        nextData.raca =
          nextSex === "femea"
            ? speciesBreeds[current.especie].female[0]
            : speciesBreeds[current.especie].male[0];

        if (nextSex === "femea") {
          nextData.ecc = current.ecc ?? 3.4;
          nextData.numero_partos = current.numero_partos ?? 0;
          nextData.abortos = current.abortos ?? 0;
          nextData.dias_desde_ultimo_parto = current.dias_desde_ultimo_parto ?? 120;
          nextData.qualidade_semen = null;
        } else {
          nextData.ecc = null;
          nextData.numero_partos = null;
          nextData.abortos = null;
          nextData.dias_desde_ultimo_parto = null;
          nextData.qualidade_semen = current.qualidade_semen ?? 3;
        }
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

  const breedOptions =
    formData.sexo === "femea"
      ? speciesBreeds[formData.especie].female
      : speciesBreeds[formData.especie].male;

  return (
    <main className="page-shell">
      <section className="hero">
        <div className="hero-copy">
          <h1>Cadastro individual de animais</h1>
          <p>
            Cadastre cada animal separadamente, informando se ele e macho ou femea. O cruzamento
            depois vai combinar uma femea como matriz e um macho como reprodutor.
          </p>
        </div>

        <div className="hero-grid">
          <article className="metric-card spotlight">
            <span>Animais disponiveis</span>
            <strong>{isHydrated ? animals.length : "..."}</strong>
            <p>Os animais cadastrados aqui abastecem o cruzamento e o painel genetico.</p>
          </article>

          <article className="metric-card">
            <span>Base reprodutiva media</span>
            <strong>{formatPercent(averageReproductiveRate)}</strong>
            <p>Leitura media do potencial reprodutivo individual do plantel atual.</p>
          </article>

          <article className="metric-card">
            <span>Composicao do cadastro</span>
            <strong>{femalesCount} femeas / {malesCount} machos</strong>
            <p>O cruzamento so e liberado quando houver pelo menos uma femea e um macho.</p>
          </article>
        </div>
      </section>

      <section className="content-grid">
        <article className="panel form-panel">
          <div className="panel-heading">
            <div>
              <h2>Novo animal</h2>
              <p>Preencha somente os dados que fazem sentido para o sexo do animal.</p>
            </div>
          </div>

          <form className="animal-form" onSubmit={handleSubmit}>
            <label>
              Nome do animal
              <input
                name="nome"
                value={formData.nome}
                onChange={handleFieldChange}
                placeholder="Ex.: Aurora 12"
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
                Sexo
                <select name="sexo" value={formData.sexo} onChange={handleFieldChange}>
                  <option value="femea">Femea</option>
                  <option value="macho">Macho</option>
                </select>
              </label>

              <label>
                Raca
                <select name="raca" value={formData.raca} onChange={handleFieldChange}>
                  {breedOptions.map((breed) => (
                    <option key={breed} value={breed}>
                      {breed}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Idade
                <input
                  name="idade"
                  type="number"
                  step="0.1"
                  value={formData.idade}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                Peso (kg)
                <input
                  name="peso_kg"
                  type="number"
                  value={formData.peso_kg}
                  onChange={handleFieldChange}
                />
              </label>

              <label>
                Filhos nascidos
                <input
                  name="filhos_nascidos"
                  type="number"
                  value={formData.filhos_nascidos}
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

              {formData.sexo === "femea" ? (
                <>
                  <label>
                    ECC da matriz
                    <input
                      name="ecc"
                      type="number"
                      step="0.1"
                      min="1"
                      max="5"
                      value={formData.ecc ?? 3.4}
                      onChange={handleFieldChange}
                    />
                  </label>

                  <label>
                    Numero de partos
                    <input
                      name="numero_partos"
                      type="number"
                      value={formData.numero_partos ?? 0}
                      onChange={handleFieldChange}
                    />
                  </label>

                  <label>
                    Abortos
                    <input
                      name="abortos"
                      type="number"
                      value={formData.abortos ?? 0}
                      onChange={handleFieldChange}
                    />
                  </label>

                  <label>
                    Dias desde o ultimo parto
                    <input
                      name="dias_desde_ultimo_parto"
                      type="number"
                      value={formData.dias_desde_ultimo_parto ?? 120}
                      onChange={handleFieldChange}
                    />
                  </label>
                </>
              ) : (
                <label>
                  Qualidade do semen
                  <input
                    name="qualidade_semen"
                    type="number"
                    min="1"
                    max="5"
                    value={formData.qualidade_semen ?? 3}
                    onChange={handleFieldChange}
                  />
                </label>
              )}
            </div>

            <button className="primary-button" type="submit">
              Adicionar ao plantel
            </button>
          </form>
        </article>

        <article className="panel">
          <div className="panel-heading">
            <div>
              <h2>Animais cadastrados</h2>
              <p>Cada registro e individual e ja indica se o animal e macho ou femea.</p>
            </div>
          </div>

          <div className="animal-list">
            {animals.map((animal) => {
              const preview = scoreAnimalReproductiveBase(animal);
              return (
                <article className="animal-card" key={animal.id}>
                  <div className="animal-card-header">
                    <div>
                      <h3>{animal.nome}</h3>
                      <p>
                        {animal.especie} - {animal.sexo} - {animal.raca}
                      </p>
                    </div>
                    <span className={`prediction-tag ${preview >= 0.5 ? "positive" : "neutral"}`}>
                      {animal.sexo === "femea" ? "Matriz" : "Reprodutor"}
                    </span>
                  </div>

                  <div className="animal-metrics">
                    <div>
                      <span>Base reprodutiva</span>
                      <strong>{formatPercent(preview)}</strong>
                    </div>
                    <div>
                      <span>Peso</span>
                      <strong>{animal.peso_kg}</strong>
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
