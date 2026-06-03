export type Species = "Bovino" | "Ovino" | "Caprino";

export type AnimalSex = "femea" | "macho";

export type GeneticGoal = "leite" | "corte" | "fertilidade";

export type AnimalRecord = {
  id: string;
  nome: string;
  especie: Species;
  sexo: AnimalSex;
  raca: string;
  idade: number;
  peso_kg: number;
  filhos_nascidos: number;
  parentesco_endogamia: number;
  ecc?: number | null;
  numero_partos?: number | null;
  abortos?: number | null;
  dias_desde_ultimo_parto?: number | null;
  qualidade_semen?: number | null;
};

export type PregnancyInput = {
  especie: Species;
  raca_matriz: string;
  idade_matriz: number;
  peso_matriz_kg: number;
  ecc_matriz: number;
  numero_partos_matriz: number;
  abortos_matriz: number;
  dias_desde_ultimo_parto: number;
  filhos_nascidos_matriz: number;
  raca_macho: string;
  idade_macho: number;
  peso_macho_kg: number;
  qualidade_semen_macho: number;
  filhos_nascidos_macho: number;
  parentesco_endogamia: number;
};

export type PredictionResult = {
  probability: number;
  predictedClass: 0 | 1;
  source: "modelo" | "fallback";
  explanation: string;
};

export type RankedAnimal = {
  animal: AnimalRecord;
  score: number;
  label: string;
  reproductiveScore: number;
};
