from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
import pandas as pd


SPECIES_CONFIG = {
    "Bovino": {
        "racas": ["Nelore", "Girolando", "Holandes", "Angus", "Gir"],
        "idade_femea": (2.0, 10.0),
        "idade_macho": (2.0, 9.0),
        "peso_femea": (350, 720),
        "peso_macho": (500, 1100),
        "partos": (0, 7),
        "filhos_femea": (0, 7),
        "filhos_macho": (0, 40),
        "dias_parto": (30, 300),
        "idade_ideal_femea": (3.0, 7.0),
        "idade_ideal_macho": (3.0, 8.0),
    },
    "Ovino": {
        "racas": ["Dorper", "Santa Ines", "Somalis", "Morada Nova", "Texel"],
        "idade_femea": (1.2, 8.0),
        "idade_macho": (1.2, 7.0),
        "peso_femea": (35, 95),
        "peso_macho": (55, 130),
        "partos": (0, 6),
        "filhos_femea": (0, 10),
        "filhos_macho": (0, 35),
        "dias_parto": (20, 220),
        "idade_ideal_femea": (2.0, 6.0),
        "idade_ideal_macho": (2.0, 6.5),
    },
    "Caprino": {
        "racas": ["Saanen", "Boer", "Anglo Nubiana", "Toggenburg", "Moxoto"],
        "idade_femea": (1.2, 8.5),
        "idade_macho": (1.2, 7.5),
        "peso_femea": (30, 85),
        "peso_macho": (45, 120),
        "partos": (0, 6),
        "filhos_femea": (0, 12),
        "filhos_macho": (0, 35),
        "dias_parto": (20, 220),
        "idade_ideal_femea": (2.0, 6.0),
        "idade_ideal_macho": (2.0, 6.5),
    },
}


def sigmoid(values: np.ndarray) -> np.ndarray:
    return 1.0 / (1.0 + np.exp(-values))


def sample_by_species(
    species: np.ndarray,
    field: str,
    rng: np.random.Generator,
    round_digits: int | None = None,
) -> np.ndarray:
    result = np.empty(len(species), dtype=object)
    for specie, config in SPECIES_CONFIG.items():
        mask = species == specie
        if not mask.any():
            continue
        low, high = config[field]
        values = rng.uniform(low, high, size=mask.sum())
        if round_digits is None:
            values = np.rint(values).astype(int)
        else:
            values = np.round(values, round_digits)
        result[mask] = values
    return result


def sample_choices_by_species(
    species: np.ndarray,
    field: str,
    rng: np.random.Generator,
) -> np.ndarray:
    result = np.empty(len(species), dtype=object)
    for specie, config in SPECIES_CONFIG.items():
        mask = species == specie
        if not mask.any():
            continue
        result[mask] = rng.choice(config[field], size=mask.sum())
    return result


def score_range(values: np.ndarray, low: np.ndarray, high: np.ndarray) -> np.ndarray:
    center = (low + high) / 2.0
    half_span = np.maximum((high - low) / 2.0, 1e-6)
    distance = np.abs(values - center) / half_span
    return np.clip(1.0 - distance, 0.0, 1.0)


def build_dataset(rows: int, seed: int) -> pd.DataFrame:
    rng = np.random.default_rng(seed)

    especie = rng.choice(["Bovino", "Ovino", "Caprino"], size=rows, p=[0.5, 0.25, 0.25])
    raca_matriz = sample_choices_by_species(especie, "racas", rng)
    raca_macho = sample_choices_by_species(especie, "racas", rng)

    idade_matriz = sample_by_species(especie, "idade_femea", rng, round_digits=1).astype(float)
    idade_macho = sample_by_species(especie, "idade_macho", rng, round_digits=1).astype(float)
    peso_matriz_kg = sample_by_species(especie, "peso_femea", rng).astype(int)
    peso_macho_kg = sample_by_species(especie, "peso_macho", rng).astype(int)
    numero_partos_matriz = sample_by_species(especie, "partos", rng).astype(int)
    filhos_nascidos_matriz = sample_by_species(especie, "filhos_femea", rng).astype(int)
    filhos_nascidos_macho = sample_by_species(especie, "filhos_macho", rng).astype(int)
    dias_desde_ultimo_parto = sample_by_species(especie, "dias_parto", rng).astype(int)

    ecc_matriz = np.round(rng.normal(3.2, 0.55, size=rows).clip(1.5, 5.0), 2)
    abortos_matriz = np.clip(rng.poisson(0.35, size=rows), 0, 4)
    qualidade_semen_macho = rng.integers(1, 6, size=rows)
    parentesco_endogamia = np.round(rng.beta(1.2, 8.0, size=rows), 3)

    idade_ideal_low_f = np.array([SPECIES_CONFIG[item]["idade_ideal_femea"][0] for item in especie])
    idade_ideal_high_f = np.array([SPECIES_CONFIG[item]["idade_ideal_femea"][1] for item in especie])
    idade_ideal_low_m = np.array([SPECIES_CONFIG[item]["idade_ideal_macho"][0] for item in especie])
    idade_ideal_high_m = np.array([SPECIES_CONFIG[item]["idade_ideal_macho"][1] for item in especie])

    s_ecc = score_range(ecc_matriz, np.full(rows, 3.0), np.full(rows, 3.75))
    s_parto = score_range(
        dias_desde_ultimo_parto.astype(float),
        np.full(rows, 70.0),
        np.full(rows, 160.0),
    )
    s_semen = (qualidade_semen_macho - 1) / 4.0
    s_idade_f = score_range(idade_matriz, idade_ideal_low_f, idade_ideal_high_f)
    s_idade_m = score_range(idade_macho, idade_ideal_low_m, idade_ideal_high_m)
    s_filhos_macho = np.clip(filhos_nascidos_macho / 40.0, 0.0, 1.0)
    s_filhos_femea = np.clip(filhos_nascidos_matriz / 12.0, 0.0, 1.0)
    s_abortos = np.clip(abortos_matriz / 4.0, 0.0, 1.0)
    s_endogamia = np.clip(parentesco_endogamia, 0.0, 1.0)

    latent_score = (
        -1.10
        + 1.20 * s_ecc
        + 0.75 * s_parto
        + 0.55 * s_semen
        + 0.45 * s_idade_f
        + 0.20 * s_idade_m
        + 0.35 * s_filhos_macho
        + 0.20 * s_filhos_femea
        - 0.75 * s_abortos
        - 0.95 * s_endogamia
    )

    chance_prenhez_gerada = sigmoid(latent_score + rng.normal(0.0, 0.25, size=rows))
    prenhou = rng.binomial(1, chance_prenhez_gerada)

    return pd.DataFrame(
        {
            "especie": especie,
            "raca_matriz": raca_matriz,
            "idade_matriz": idade_matriz,
            "peso_matriz_kg": peso_matriz_kg,
            "ecc_matriz": ecc_matriz,
            "numero_partos_matriz": numero_partos_matriz,
            "abortos_matriz": abortos_matriz,
            "dias_desde_ultimo_parto": dias_desde_ultimo_parto,
            "filhos_nascidos_matriz": filhos_nascidos_matriz,
            "raca_macho": raca_macho,
            "idade_macho": idade_macho,
            "peso_macho_kg": peso_macho_kg,
            "qualidade_semen_macho": qualidade_semen_macho,
            "filhos_nascidos_macho": filhos_nascidos_macho,
            "parentesco_endogamia": parentesco_endogamia,
            "chance_prenhez_gerada": np.round(chance_prenhez_gerada, 4),
            "prenhou": prenhou,
        }
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate a synthetic pregnancy dataset.")
    parser.add_argument(
        "--rows",
        type=int,
        default=10_000,
        help="Number of rows to generate.",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=42,
        help="Random seed used for reproducible generation.",
    )
    parser.add_argument(
        "--output",
        default="data/raw/dataset.csv",
        help="Path where the generated CSV will be saved.",
    )
    args = parser.parse_args()

    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)

    dataset = build_dataset(rows=args.rows, seed=args.seed)
    dataset.to_csv(output_path, index=False)

    print(f"Dataset generated with {len(dataset)} rows at: {output_path.resolve()}")


if __name__ == "__main__":
    main()
