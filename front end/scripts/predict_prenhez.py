from __future__ import annotations

import json
import sys
from pathlib import Path


def main() -> None:
    root_dir = Path(__file__).resolve().parents[2]
    sys.path.insert(0, root_dir.as_posix())

    from src.inference import predict_pregnancy

    payload = json.loads(sys.stdin.read())
    model_path = root_dir / "models" / "random_forest_prenhez.onnx"
    result = predict_pregnancy(payload, model_path=model_path)

    result = {
      "probability": result["probability"],
      "predictedClass": result["predicted_class"],
      "explanation": "Predicao realizada com o modelo ONNX salvo em models/random_forest_prenhez.onnx.",
    }

    sys.stdout.write(json.dumps(result))


if __name__ == "__main__":
    main()
