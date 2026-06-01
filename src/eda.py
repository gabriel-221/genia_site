from __future__ import annotations

import argparse
from pathlib import Path

import pandas as pd


def build_summary(df: pd.DataFrame, target: str) -> str:
    lines: list[str] = []
    lines.append("Dataset summary")
    lines.append(f"Rows: {len(df)}")
    lines.append(f"Columns: {len(df.columns)}")
    lines.append("")
    lines.append("Column types:")
    lines.append(df.dtypes.astype(str).to_string())
    lines.append("")
    lines.append("Missing values:")
    lines.append(df.isna().sum().sort_values(ascending=False).to_string())
    lines.append("")

    if target in df.columns:
        lines.append(f"Target distribution: {target}")
        lines.append(df[target].value_counts(dropna=False).to_string())
        lines.append("")

    numeric_cols = df.select_dtypes(include="number").columns.tolist()
    if numeric_cols:
        lines.append("Numeric describe:")
        lines.append(df[numeric_cols].describe().to_string())

    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description="Run a simple exploratory analysis.")
    parser.add_argument("--input", required=True, help="Path to the input CSV file.")
    parser.add_argument("--target", default="prenhou", help="Target column name.")
    parser.add_argument(
        "--reports-dir",
        default="reports",
        help="Directory where analysis artifacts will be written.",
    )
    args = parser.parse_args()

    input_path = Path(args.input)
    reports_dir = Path(args.reports_dir)
    reports_dir.mkdir(parents=True, exist_ok=True)

    if not input_path.exists():
        raise FileNotFoundError(f"Input file not found: {input_path}")

    df = pd.read_csv(input_path)

    summary = build_summary(df, args.target)
    (reports_dir / "eda_summary.txt").write_text(summary, encoding="utf-8")

    if args.target in df.columns:
        distribution = (
            df[args.target]
            .value_counts(dropna=False)
            .rename_axis(args.target)
            .reset_index(name="count")
        )
        distribution.to_csv(reports_dir / "target_distribution.csv", index=False)

    print(f"EDA finished. Artifacts available in: {reports_dir.resolve()}")


if __name__ == "__main__":
    main()
