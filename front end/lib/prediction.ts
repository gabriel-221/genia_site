import { estimatePregnancyLocally } from "@/lib/scoring";
import { AnimalRecord, PregnancyInput, PredictionResult } from "@/lib/types";

export function buildId(name: string) {
  return `${name.toLowerCase().replace(/\s+/g, "-")}-${Date.now()}`;
}

export function formatPercent(value: number) {
  return `${(value * 100).toFixed(1)}%`;
}

export function buildCrossingPayload(female: AnimalRecord, male: AnimalRecord): PregnancyInput {
  if (female.sexo !== "femea") {
    throw new Error("A matriz do cruzamento precisa ser uma femea.");
  }

  if (male.sexo !== "macho") {
    throw new Error("O reprodutor do cruzamento precisa ser um macho.");
  }

  return {
    especie: female.especie,
    raca_matriz: female.raca,
    idade_matriz: female.idade,
    peso_matriz_kg: female.peso_kg,
    ecc_matriz: female.ecc ?? 3,
    numero_partos_matriz: female.numero_partos ?? 0,
    abortos_matriz: female.abortos ?? 0,
    dias_desde_ultimo_parto: female.dias_desde_ultimo_parto ?? 120,
    filhos_nascidos_matriz: female.filhos_nascidos,
    raca_macho: male.raca,
    idade_macho: male.idade,
    peso_macho_kg: male.peso_kg,
    qualidade_semen_macho: male.qualidade_semen ?? 3,
    filhos_nascidos_macho: male.filhos_nascidos,
    parentesco_endogamia: Number(
      ((female.parentesco_endogamia + male.parentesco_endogamia) / 2).toFixed(2),
    ),
  };
}

export async function requestPrediction(animal: PregnancyInput) {
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
