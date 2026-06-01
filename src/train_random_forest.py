from __future__ import annotations

import argparse
from pathlib import Path

import joblib
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.impute import SimpleImputer
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder


def build_pipeline(X: pd.DataFrame, n_jobs: int, n_estimators: int) -> Pipeline:
    numeric_features = X.select_dtypes(include="number").columns.tolist()
    categorical_features = X.select_dtypes(exclude="number").columns.tolist()

    numeric_transformer = Pipeline(
        steps=[("imputer", SimpleImputer(strategy="median"))]
    )

    categorical_transformer = Pipeline(
        steps=[
            ("imputer", SimpleImputer(strategy="most_frequent")),
            ("onehot", OneHotEncoder(handle_unknown="ignore")),
        ]
    )

    preprocessor = ColumnTransformer(
        transformers=[
            ("num", numeric_transformer, numeric_features),
            ("cat", categorical_transformer, categorical_features),
        ]
    )

    model = RandomForestClassifier(
        n_estimators=n_estimators,
        max_depth=None,
        min_samples_split=2,
        min_samples_leaf=1,
        random_state=42,
        n_jobs=n_jobs,
        class_weight="balanced",
    )

    return Pipeline(steps=[("preprocessor", preprocessor), ("model", model)])


def write_metrics(
    y_full: pd.Series,
    y_train: pd.Series,
    y_test: pd.Series,
    predictions: pd.Series,
    prediction_probabilities: pd.Series,
    reports_dir: Path,
) -> None:
    accuracy = accuracy_score(y_test, predictions)
    matrix = confusion_matrix(y_test, predictions)
    report = classification_report(y_test, predictions)
    observed_rate_full = y_full.mean()
    observed_rate_train = y_train.mean()
    observed_rate_test = y_test.mean()
    predicted_positive_rate = predictions.mean()
    average_predicted_probability = prediction_probabilities.mean()

    content = "\n".join(
        [
            "Pregnancy rate summary:",
            f"Observed pregnancy rate (full dataset): {observed_rate_full:.4%}",
            f"Observed pregnancy rate (train split): {observed_rate_train:.4%}",
            f"Observed pregnancy rate (test split): {observed_rate_test:.4%}",
            f"Predicted pregnancy rate (test split): {predicted_positive_rate:.4%}",
            f"Average predicted probability (test split): {average_predicted_probability:.4%}",
            "",
            f"Accuracy: {accuracy:.4f}",
            "",
            "Confusion matrix:",
            str(matrix),
            "",
            "Classification report:",
            report,
        ]
    )
    (reports_dir / "model_metrics.txt").write_text(content, encoding="utf-8")


def write_feature_importance(
    pipeline: Pipeline,
    X_train: pd.DataFrame,
    reports_dir: Path,
) -> None:
    preprocessor: ColumnTransformer = pipeline.named_steps["preprocessor"]
    model: RandomForestClassifier = pipeline.named_steps["model"]

    feature_names = preprocessor.get_feature_names_out()
    importance_df = pd.DataFrame(
        {
            "feature": feature_names,
            "importance": model.feature_importances_,
        }
    ).sort_values("importance", ascending=False)

    importance_df.to_csv(reports_dir / "feature_importance.csv", index=False)


def main() -> None:
    parser = argparse.ArgumentParser(description="Train a Random Forest classifier.")
    parser.add_argument("--input", required=True, help="Path to the input CSV file.")
    parser.add_argument("--target", default="prenhou", help="Target column name.")
    parser.add_argument(
        "--models-dir",
        default="models",
        help="Directory where the trained model will be saved.",
    )
    parser.add_argument(
        "--reports-dir",
        default="reports",
        help="Directory where training artifacts will be written.",
    )
    parser.add_argument(
        "--test-size",
        type=float,
        default=0.2,
        help="Fraction reserved for the test split.",
    )
    parser.add_argument(
        "--n-jobs",
        type=int,
        default=1,
        help="Number of parallel jobs used by Random Forest.",
    )
    parser.add_argument(
        "--n-estimators",
        type=int,
        default=120,
        help="Number of trees used by Random Forest.",
    )
    parser.add_argument(
        "--compress",
        type=int,
        default=3,
        help="Joblib compression level used when saving the trained model.",
    )
    parser.add_argument(
        "--drop-columns",
        nargs="*",
        default=["chance_prenhez_gerada"],
        help="Columns removed from the feature matrix before training.",
    )
    args = parser.parse_args()

    input_path = Path(args.input)
    models_dir = Path(args.models_dir)
    reports_dir = Path(args.reports_dir)
    models_dir.mkdir(parents=True, exist_ok=True)
    reports_dir.mkdir(parents=True, exist_ok=True)

    if not input_path.exists():
        raise FileNotFoundError(f"Input file not found: {input_path}")

    df = pd.read_csv(input_path)
    if args.target not in df.columns:
        raise ValueError(f"Target column '{args.target}' not found in dataset.")

    columns_to_drop = [args.target, *args.drop_columns]
    X = df.drop(columns=[column for column in columns_to_drop if column in df.columns])
    y = df[args.target]

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=args.test_size,
        random_state=42,
        stratify=y if y.nunique() > 1 else None,
    )

    pipeline = build_pipeline(X, n_jobs=args.n_jobs, n_estimators=args.n_estimators)
    pipeline.fit(X_train, y_train)

    predictions = pipeline.predict(X_test)
    prediction_probabilities = pipeline.predict_proba(X_test)[:, 1]
    write_metrics(
        y_full=y,
        y_train=y_train,
        y_test=y_test,
        predictions=predictions,
        prediction_probabilities=pd.Series(prediction_probabilities),
        reports_dir=reports_dir,
    )
    write_feature_importance(pipeline, X_train, reports_dir)

    model_path = models_dir / "random_forest_prenhez.joblib"
    joblib.dump(pipeline, model_path, compress=args.compress)

    print(f"Training finished. Model saved at: {model_path.resolve()}")


if __name__ == "__main__":
    main()
