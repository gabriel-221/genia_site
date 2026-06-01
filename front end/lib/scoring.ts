import { AnimalRecord, GeneticGoal, RankedAnimal } from "@/lib/types";

const SPECIES_IDEAL_AGES = {
  Bovino: {
    female: [3.0, 7.0],
    male: [3.0, 8.0],
  },
  Ovino: {
    female: [2.0, 6.0],
    male: [2.0, 6.5],
  },
  Caprino: {
    female: [2.0, 6.0],
    male: [2.0, 6.5],
  },
} as const;

const GOAL_BREED_BONUS: Record<GeneticGoal, string[]> = {
  leite: ["Girolando", "Holandes", "Gir", "Saanen", "Toggenburg"],
  corte: ["Angus", "Nelore", "Brahman", "Senepol", "Boer", "Dorper", "Texel"],
  fertilidade: ["Gir", "Girolando", "Santa Ines", "Moxoto", "Anglo Nubiana"],
};

function clamp(value: number, min: number, max: number) {
  return Math.min(Math.max(value, min), max);
}

function sigmoid(value: number) {
  return 1 / (1 + Math.exp(-value));
}

function scoreRange(value: number, low: number, high: number) {
  const center = (low + high) / 2;
  const halfSpan = Math.max((high - low) / 2, 0.0001);
  const distance = Math.abs(value - center) / halfSpan;
  return clamp(1 - distance, 0, 1);
}

function computeBreedBonus(animal: AnimalRecord, goal: GeneticGoal) {
  const values = GOAL_BREED_BONUS[goal];
  let total = 0;

  if (values.includes(animal.raca_matriz)) {
    total += 0.12;
  }
  if (values.includes(animal.raca_macho)) {
    total += 0.08;
  }

  return total;
}

export function estimatePregnancyLocally(animal: AnimalRecord) {
  const speciesAges = SPECIES_IDEAL_AGES[animal.especie];

  const s_ecc = scoreRange(animal.ecc_matriz, 3.0, 3.75);
  const s_parto = scoreRange(animal.dias_desde_ultimo_parto, 70, 160);
  const s_semen = clamp((animal.qualidade_semen_macho - 1) / 4, 0, 1);
  const s_idade_f = scoreRange(
    animal.idade_matriz,
    speciesAges.female[0],
    speciesAges.female[1],
  );
  const s_idade_m = scoreRange(
    animal.idade_macho,
    speciesAges.male[0],
    speciesAges.male[1],
  );
  const s_filhos_macho = clamp(animal.filhos_nascidos_macho / 40, 0, 1);
  const s_filhos_femea = clamp(animal.filhos_nascidos_matriz / 12, 0, 1);
  const s_abortos = clamp(animal.abortos_matriz / 4, 0, 1);
  const s_endogamia = clamp(animal.parentesco_endogamia, 0, 1);

  const latentScore =
    -1.1 +
    1.2 * s_ecc +
    0.75 * s_parto +
    0.55 * s_semen +
    0.45 * s_idade_f +
    0.2 * s_idade_m +
    0.35 * s_filhos_macho +
    0.2 * s_filhos_femea -
    0.75 * s_abortos -
    0.95 * s_endogamia;

  const probability = sigmoid(latentScore);

  return {
    probability,
    predictedClass: (probability >= 0.5 ? 1 : 0) as 0 | 1,
    source: "fallback" as const,
    explanation: "Estimativa local baseada na formula sintetica do projeto.",
  };
}

export function scoreAnimalReproductiveBase(animal: AnimalRecord) {
  const speciesAges = SPECIES_IDEAL_AGES[animal.especie];
  const eccScore = clamp(animal.ecc_matriz / 5, 0, 1);
  const semen = clamp(animal.qualidade_semen_macho / 5, 0, 1);
  const prolificacy = clamp(animal.filhos_nascidos_matriz / 12, 0, 1);
  const fertilityPenalty = 1 - clamp(animal.parentesco_endogamia * 1.5, 0, 0.5);
  const abortoPenalty = 1 - clamp(animal.abortos_matriz / 4, 0, 0.6);
  const ageFemaleScore = scoreRange(
    animal.idade_matriz,
    speciesAges.female[0],
    speciesAges.female[1],
  );
  const ageMaleScore = scoreRange(
    animal.idade_macho,
    speciesAges.male[0],
    speciesAges.male[1],
  );

  return clamp(
    0.24 * eccScore +
      0.18 * semen +
      0.18 * prolificacy +
      0.16 * fertilityPenalty +
      0.14 * abortoPenalty +
      0.1 * ageFemaleScore +
      0.1 * ageMaleScore,
    0,
    1,
  );
}

export function scoreAnimalForGoal(
  animal: AnimalRecord,
  goal: GeneticGoal,
) {
  const breedBonus = computeBreedBonus(animal, goal);
  const eccScore = clamp(animal.ecc_matriz / 5, 0, 1);
  const femaleWeightScore = clamp(animal.peso_matriz_kg / 700, 0, 1);
  const maleWeightScore = clamp(animal.peso_macho_kg / 1100, 0, 1);
  const fertilityPenalty = 1 - clamp(animal.parentesco_endogamia * 1.5, 0, 0.5);
  const abortoPenalty = 1 - clamp(animal.abortos_matriz / 4, 0, 0.6);
  const prolificacy = clamp(animal.filhos_nascidos_matriz / 12, 0, 1);
  const semen = clamp(animal.qualidade_semen_macho / 5, 0, 1);
  const reproductiveScore = scoreAnimalReproductiveBase(animal);

  let baseScore = 0;

  if (goal === "leite") {
    baseScore =
      0.35 * eccScore +
      0.25 * prolificacy +
      0.18 * reproductiveScore +
      0.12 * femaleWeightScore +
      0.1 * fertilityPenalty;
  }

  if (goal === "corte") {
    baseScore =
      0.35 * femaleWeightScore +
      0.3 * maleWeightScore +
      0.15 * eccScore +
      0.1 * reproductiveScore +
      0.1 * semen;
  }

  if (goal === "fertilidade") {
    baseScore =
      0.38 * reproductiveScore +
      0.22 * eccScore +
      0.16 * semen +
      0.14 * abortoPenalty +
      0.1 * fertilityPenalty;
  }

  return clamp((baseScore + breedBonus) * 100, 0, 100);
}

export function getScoreLabel(score: number) {
  if (score >= 82) {
    return "Elite genetica";
  }
  if (score >= 68) {
    return "Alto potencial";
  }
  if (score >= 54) {
    return "Bom desempenho";
  }
  return "Monitorar";
}

export function rankAnimals(
  animals: AnimalRecord[],
  goal: GeneticGoal,
) {
  return [...animals]
    .map((animal) => {
      const reproductiveScore = scoreAnimalReproductiveBase(animal);
      const score = scoreAnimalForGoal(animal, goal);

      return {
        animal,
        score,
        label: getScoreLabel(score),
        reproductiveScore,
      } satisfies RankedAnimal;
    })
    .sort((left, right) => right.score - left.score);
}
