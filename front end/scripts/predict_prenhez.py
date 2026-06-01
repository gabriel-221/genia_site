from __future__ import annotations

import json
import sys
from pathlib import Path

import joblib
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


def main() -> None:
    payload = json.loads(sys.stdin.read())
    root_dir = Path(__file__).resolve().parents[2]
    model_path = root_dir / "models" / "random_forest_prenhez.joblib"

    if not model_path.exists():
        raise FileNotFoundError(f"Modelo nao encontrado em: {model_path}")

    pipeline = joblib.load(model_path)
    row = {column: payload[column] for column in MODEL_COLUMNS}
    frame = pd.DataFrame([row], columns=MODEL_COLUMNS)

    probability = float(pipeline.predict_proba(frame)[0][1])
    predicted_class = int(pipeline.predict(frame)[0])

    result = {
      "probability": probability,
      "predictedClass": predicted_class,
      "explanation": "Predicao realizada com o pipeline Random Forest salvo em models/random_forest_prenhez.joblib.",
    }

    sys.stdout.write(json.dumps(result))


if __name__ == "__main__":
    main()
