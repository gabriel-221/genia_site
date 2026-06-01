import { AnimalRecord, GeneticGoal } from "@/lib/types";

export const speciesBreeds: Record<AnimalRecord["especie"], { female: string[]; male: string[] }> =
  {
    Bovino: {
      female: ["Nelore", "Girolando", "Holandes", "Angus", "Gir"],
      male: ["Nelore", "Angus", "Gir", "Brahman", "Senepol"],
    },
    Ovino: {
      female: ["Dorper", "Santa Ines", "Somalis", "Morada Nova", "Texel"],
      male: ["Dorper", "Texel", "Santa Ines", "Ile de France", "Suffolk"],
    },
    Caprino: {
      female: ["Saanen", "Boer", "Anglo Nubiana", "Toggenburg", "Moxoto"],
      male: ["Boer", "Saanen", "Alpina", "Anglo Nubiana", "Toggenburg"],
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
  raca_matriz: "Girolando",
  idade_matriz: 4.5,
  peso_matriz_kg: 500,
  ecc_matriz: 3.4,
  numero_partos_matriz: 2,
  abortos_matriz: 0,
  dias_desde_ultimo_parto: 110,
  filhos_nascidos_matriz: 2,
  raca_macho: "Gir",
  idade_macho: 4.2,
  peso_macho_kg: 760,
  qualidade_semen_macho: 4,
  filhos_nascidos_macho: 16,
  parentesco_endogamia: 0.04,
};
