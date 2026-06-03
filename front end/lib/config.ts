import { AnimalRecord, GeneticGoal } from "@/lib/types";

export const speciesBreeds: Record<AnimalRecord["especie"], { female: string[]; male: string[] }> =
  {
    Bovino: {
      female: ["Nelore", "Girolando", "Holandes", "Angus", "Gir"],
      male: ["Nelore", "Girolando", "Holandes", "Angus", "Gir"],
    },
    Ovino: {
      female: ["Dorper", "Santa Ines", "Somalis", "Morada Nova", "Texel"],
      male: ["Dorper", "Santa Ines", "Somalis", "Morada Nova", "Texel"],
    },
    Caprino: {
      female: ["Saanen", "Boer", "Anglo Nubiana", "Toggenburg", "Moxoto"],
      male: ["Saanen", "Boer", "Anglo Nubiana", "Toggenburg", "Moxoto"],
    },
  };

export const goalLabels: Record<GeneticGoal, string> = {
  leite: "Producao de leite",
  corte: "Peso para corte",
  fertilidade: "Fertilidade",
};

export const baseForm: AnimalRecord = {
  id: "",
  nome: "",
  especie: "Bovino",
  sexo: "femea",
  raca: "Girolando",
  idade: 4.5,
  peso_kg: 500,
  filhos_nascidos: 2,
  parentesco_endogamia: 0.04,
  ecc: 3.4,
  numero_partos: 2,
  abortos: 0,
  dias_desde_ultimo_parto: 110,
  qualidade_semen: null,
};
