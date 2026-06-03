"use client";

import { initialAnimals } from "@/lib/animal-data";
import { AnimalRecord } from "@/lib/types";
import { useEffect, useState } from "react";

const STORAGE_KEY = "painel-genetico-rural:animals";

function isAnimalRecord(value: unknown): value is AnimalRecord {
  if (!value || typeof value !== "object") {
    return false;
  }

  const animal = value as Partial<AnimalRecord>;
  return (
    typeof animal.id === "string" &&
    typeof animal.nome === "string" &&
    typeof animal.especie === "string" &&
    (animal.sexo === "femea" || animal.sexo === "macho") &&
    typeof animal.raca === "string" &&
    typeof animal.idade === "number" &&
    typeof animal.peso_kg === "number"
  );
}

function normalizeAnimals(value: unknown): AnimalRecord[] {
  if (!Array.isArray(value)) {
    return initialAnimals;
  }

  const validAnimals = value.filter(isAnimalRecord);
  return validAnimals.length ? validAnimals : initialAnimals;
}

export function useAnimalStore() {
  const [animals, setAnimals] = useState<AnimalRecord[]>(initialAnimals);
  const [isHydrated, setIsHydrated] = useState(false);

  useEffect(() => {
    try {
      const stored = window.localStorage.getItem(STORAGE_KEY);
      if (stored) {
        setAnimals(normalizeAnimals(JSON.parse(stored)));
      }
    } catch {
      setAnimals(initialAnimals);
    } finally {
      setIsHydrated(true);
    }
  }, []);

  useEffect(() => {
    if (!isHydrated) {
      return;
    }

    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(animals));
  }, [animals, isHydrated]);

  return { animals, setAnimals, isHydrated };
}
