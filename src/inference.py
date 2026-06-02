from __future__ import annotations

from pathlib import Path
from typing import Any

import numpy as np
import onnxruntime as ort
import pandas as pd


MODEL_COLUMNS = [
    "especie",
    "raca_matriz",
    "idade_matriz",
    "peso_matriz_kg",
    "ecc_matriz",
    "numero_partos_matriz",
    "abortos_matriz",
    "dias_desde_ultimo_parto",
    "filhos_nascidos_matriz",
    "raca_macho",
    "idade_macho",
    "peso_macho_kg",
    "qualidade_semen_macho",
    "filhos_nascidos_macho",
    "parentesco_endogamia",
]

STRING_COLUMNS = [
    "especie",
    "raca_matriz",
    "raca_macho",
]

NUMERIC_COLUMNS = [column for column in MODEL_COLUMNS if column not in STRING_COLUMNS]


def load_pregnancy_model(model_path: str | Path = "models/random_forest_prenhez.onnx") -> ort.InferenceSession:
    path = Path(model_path)
    if not path.exists():
        raise FileNotFoundError(f"Modelo nao encontrado em: {path}")
    return ort.InferenceSession(path.as_posix(), providers=["CPUExecutionProvider"])


def build_onnx_inputs(animal_data: dict[str, Any]) -> dict[str, np.ndarray]:
    row = {column: animal_data[column] for column in MODEL_COLUMNS}
    frame = pd.DataFrame([row], columns=MODEL_COLUMNS)
    inputs: dict[str, np.ndarray] = {}

    for column in STRING_COLUMNS:
        inputs[column] = frame[[column]].astype(str).to_numpy(dtype=object)

    for column in NUMERIC_COLUMNS:
        inputs[column] = frame[[column]].astype("float32").to_numpy(dtype=np.float32)

    return inputs


def predict_pregnancy(
    animal_data: dict[str, Any],
    model_path: str | Path = "models/random_forest_prenhez.onnx",
) -> dict[str, float | int]:
    session = load_pregnancy_model(model_path)
    outputs = session.run(None, build_onnx_inputs(animal_data))
    predicted_class = int(outputs[0][0])
    probability = float(outputs[1][0][1])

    return {
        "probability": probability,
        "predicted_class": predicted_class,
    }
