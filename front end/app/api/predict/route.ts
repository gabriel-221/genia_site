import { estimatePregnancyLocally } from "@/lib/scoring";
import { AnimalRecord } from "@/lib/types";
import { spawn } from "node:child_process";
import { resolve } from "node:path";

export const runtime = "nodejs";

async function runPythonPrediction(payload: AnimalRecord) {
  const scriptPath = resolve(process.cwd(), "scripts", "predict_prenhez.py");

  return new Promise<string>((resolvePromise, rejectPromise) => {
    const child = spawn("python", [scriptPath], {
      cwd: process.cwd(),
      stdio: ["pipe", "pipe", "pipe"],
    });

    let stdout = "";
    let stderr = "";

    child.stdout.on("data", (chunk) => {
      stdout += chunk.toString();
    });

    child.stderr.on("data", (chunk) => {
      stderr += chunk.toString();
    });

    child.on("error", (error) => {
      rejectPromise(error);
    });

    child.on("close", (code) => {
      if (code === 0) {
        resolvePromise(stdout);
        return;
      }

      rejectPromise(new Error(stderr || `Python finalizou com codigo ${code}`));
    });

    child.stdin.write(JSON.stringify(payload));
    child.stdin.end();
  });
}

export async function POST(request: Request) {
  const payload = (await request.json()) as AnimalRecord;

  try {
    const stdout = await runPythonPrediction(payload);
    const result = JSON.parse(stdout) as {
      probability: number;
      predictedClass: 0 | 1;
      explanation: string;
    };

    return Response.json({
      ...result,
      source: "modelo",
    });
  } catch {
    const fallback = estimatePregnancyLocally(payload);
    return Response.json(fallback);
  }
}
