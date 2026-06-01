"use client";

import { initialAnimals } from "@/lib/animal-data";
import { AnimalRecord } from "@/lib/types";
import { useEffect, useState } from "react";

const STORAGE_KEY = "painel-genetico-rural:animals";

export function useAnimalStore() {
  const [animals, setAnimals] = useState<AnimalRecord[]>(initialAnimals);
  const [isHydrated, setIsHydrated] = useState(false);

  useEffect(() => {
    try {
      const stored = window.localStorage.getItem(STORAGE_KEY);
      if (stored) {
        setAnimals(JSON.parse(stored) as AnimalRecord[]);
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
