import { estimatePregnancyLocally } from "@/lib/scoring";
import { AnimalRecord, PredictionResult } from "@/lib/types";

export function buildId(name: string) {
  return `${name.toLowerCase().replace(/\s+/g, "-")}-${Date.now()}`;
}

export function formatPercent(value: number) {
  return `${(value * 100).toFixed(1)}%`;
}

export function buildCrossingPayload(female: AnimalRecord, male: AnimalRecord): AnimalRecord {
  return {
    ...female,
    id: `${female.id}-${male.id}`,
    nome: `${female.nome} x ${male.nome}`,
    raca_macho: male.raca_matriz,
    idade_macho: male.idade_matriz,
    peso_macho_kg: male.peso_matriz_kg,
    qualidade_semen_macho: male.qualidade_semen_macho,
    filhos_nascidos_macho: male.filhos_nascidos_matriz,
    parentesco_endogamia: Number(
      ((female.parentesco_endogamia + male.parentesco_endogamia) / 2).toFixed(2),
    ),
  };
}

export async function requestPrediction(animal: AnimalRecord) {
  try {
    const response = await fetch("/api/predict", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(animal),
    });

    if (!response.ok) {
      throw new Error("Falha ao consultar a API.");
    }

    return (await response.json()) as PredictionResult;
  } catch {
    return estimatePregnancyLocally(animal);
  }
}
