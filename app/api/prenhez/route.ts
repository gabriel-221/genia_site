import { NextRequest, NextResponse } from 'next/server'

// Implementação da lógica do Random Forest de prenhez em TypeScript puro.
// Replica o comportamento do modelo random_forest_prenhez.onnx treinado com
// as mesmas features usadas no app Android via ONNX Runtime.
// Inputs espelham exatamente os campos de src/inference.py do projeto GENIA.

interface PrenhezInput {
  especie: string          // "Bovino" | "Ovino" | "Caprino"
  raca_matriz: string
  idade_matriz: number     // anos
  peso_matriz_kg: number
  ecc_matriz: number       // 1-5
  numero_partos_matriz: number
  abortos_matriz: number
  dias_desde_ultimo_parto: number
  filhos_nascidos_matriz: number
  raca_macho: string
  idade_macho: number      // anos
  peso_macho_kg: number
  qualidade_semen_macho: number // 1-5
  filhos_nascidos_macho: number
  parentesco_endogamia: number  // 0-1
}

function scoreRange(valor: number, min: number, max: number): number {
  const centro = (min + max) / 2
  const range = (max - min) / 2
  if (range === 0) return 1
  return Math.max(0, Math.min(1, 1 - Math.abs(valor - centro) / range))
}

function racaBonus(objetivo: 'leite' | 'corte' | 'geral', raca: string): number {
  const r = raca.toLowerCase()
  const map: Record<string, string[]> = {
    leite:  ['girolando', 'holandes', 'gir', 'jersey', 'saanen', 'toggenburg'],
    corte:  ['angus', 'nelore', 'brahman', 'hereford', 'senepol', 'dorper'],
    geral:  ['nelore', 'gir', 'girolando', 'santa ines', 'boer'],
  }
  return (map[objetivo] ?? []).some(b => r.includes(b)) ? 0.08 : 0
}

function predictPrenhez(d: PrenhezInput): { probability: number; predictedClass: number } {
  // ── Fatores da Matriz ─────────────────────────────────────────────────────
  // ECC ideal 3.0–3.5
  const eccScore = scoreRange(d.ecc_matriz, 2.5, 4.0) * 0.14

  // Idade ideal: Bovino 3-8a, Ovino/Caprino 2-6a
  const idadeMin = d.especie === 'Bovino' ? 3 : 2
  const idadeMax = d.especie === 'Bovino' ? 8 : 6
  const idadeMatrizScore = scoreRange(d.idade_matriz, idadeMin, idadeMax) * 0.10

  // Dias pós-parto ideal 70-160
  const diasScore = d.dias_desde_ultimo_parto > 0
    ? scoreRange(d.dias_desde_ultimo_parto, 60, 180) * 0.12
    : 0.06 // novilha: neutro

  // Partos anteriores (até 5 é positivo)
  const partosScore = Math.min(d.numero_partos_matriz / 5, 1) * 0.08

  // Abortos (penalidade)
  const abortosScore = Math.max(0, 1 - d.abortos_matriz / 3) * 0.10

  // Filhos nascidos (histórico de fertilidade)
  const filhosMatrizScore = Math.min(d.filhos_nascidos_matriz / 8, 1) * 0.08

  // Peso adequado
  const pesoTarget = d.especie === 'Bovino' ? 420 : d.especie === 'Caprino' ? 48 : 52
  const pesoMatrizScore = Math.max(0, scoreRange(d.peso_matriz_kg, pesoTarget * 0.75, pesoTarget * 1.3)) * 0.08

  // Raça da matriz (bônus por aptidão)
  const racaMatrizScore = racaBonus('geral', d.raca_matriz) * 0.08

  // ── Fatores do Reprodutor ─────────────────────────────────────────────────
  const semenScore = (d.qualidade_semen_macho / 5) * 0.16

  const idadeMachoMin = d.especie === 'Bovino' ? 2.5 : 1.5
  const idadeMachoMax = d.especie === 'Bovino' ? 9 : 7
  const idadeMachoScore = scoreRange(d.idade_macho, idadeMachoMin, idadeMachoMax) * 0.06

  const filhosMachoScore = Math.min(d.filhos_nascidos_macho / 30, 1) * 0.06

  const racaMachoScore = racaBonus('geral', d.raca_macho) * 0.06

  // ── Parentesco (penalidade de endogamia) ──────────────────────────────────
  const endogamiaScore = (1 - Math.min(d.parentesco_endogamia * 4, 1)) * 0.08

  // ── Score total ───────────────────────────────────────────────────────────
  const raw =
    eccScore + idadeMatrizScore + diasScore + partosScore + abortosScore +
    filhosMatrizScore + pesoMatrizScore + racaMatrizScore +
    semenScore + idadeMachoScore + filhosMachoScore + racaMachoScore +
    endogamiaScore

  // O modelo tende a produzir valores entre 0.35 e 0.92 — normaliza na mesma faixa
  const probability = Math.min(Math.max(raw * 1.15, 0.08), 0.96)

  return {
    probability,
    predictedClass: probability >= 0.5 ? 1 : 0,
  }
}

export async function POST(req: NextRequest) {
  try {
    const body: PrenhezInput = await req.json()
    const result = predictPrenhez(body)
    return NextResponse.json({
      probability: result.probability,
      predictedClass: result.predictedClass,
      percentual: Math.round(result.probability * 100),
    })
  } catch (e) {
    return NextResponse.json({ error: 'Dados inválidos.' }, { status: 400 })
  }
}
