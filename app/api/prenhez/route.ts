import { NextRequest, NextResponse } from 'next/server'

interface PrenhezInput {
  especie: string
  raca_matriz: string
  idade_matriz: number
  peso_matriz_kg: number
  ecc_matriz: number
  numero_partos_matriz: number
  abortos_matriz: number
  dias_desde_ultimo_parto: number
  filhos_nascidos_matriz: number
  raca_macho: string
  idade_macho: number
  peso_macho_kg: number
  qualidade_semen_macho: number
  filhos_nascidos_macho: number
  parentesco_endogamia: number
}

function validateInput(b: unknown): b is PrenhezInput {
  if (typeof b !== 'object' || b === null) return false
  const d = b as Record<string, unknown>
  return (
    typeof d.especie === 'string' &&
    ['Bovino', 'Ovino', 'Caprino'].includes(d.especie) &&
    typeof d.raca_matriz === 'string' && d.raca_matriz.length > 0 && d.raca_matriz.length <= 100 &&
    typeof d.raca_macho === 'string' && d.raca_macho.length > 0 && d.raca_macho.length <= 100 &&
    typeof d.idade_matriz === 'number' && isFinite(d.idade_matriz) && d.idade_matriz >= 0 && d.idade_matriz <= 30 &&
    typeof d.peso_matriz_kg === 'number' && isFinite(d.peso_matriz_kg) && d.peso_matriz_kg >= 0 && d.peso_matriz_kg <= 2000 &&
    typeof d.ecc_matriz === 'number' && isFinite(d.ecc_matriz) && d.ecc_matriz >= 1 && d.ecc_matriz <= 5 &&
    typeof d.numero_partos_matriz === 'number' && isFinite(d.numero_partos_matriz) && d.numero_partos_matriz >= 0 && d.numero_partos_matriz <= 30 &&
    typeof d.abortos_matriz === 'number' && isFinite(d.abortos_matriz) && d.abortos_matriz >= 0 && d.abortos_matriz <= 20 &&
    typeof d.dias_desde_ultimo_parto === 'number' && isFinite(d.dias_desde_ultimo_parto) && d.dias_desde_ultimo_parto >= 0 && d.dias_desde_ultimo_parto <= 3650 &&
    typeof d.filhos_nascidos_matriz === 'number' && isFinite(d.filhos_nascidos_matriz) && d.filhos_nascidos_matriz >= 0 && d.filhos_nascidos_matriz <= 50 &&
    typeof d.idade_macho === 'number' && isFinite(d.idade_macho) && d.idade_macho >= 0 && d.idade_macho <= 30 &&
    typeof d.peso_macho_kg === 'number' && isFinite(d.peso_macho_kg) && d.peso_macho_kg >= 0 && d.peso_macho_kg <= 2000 &&
    typeof d.qualidade_semen_macho === 'number' && isFinite(d.qualidade_semen_macho) && d.qualidade_semen_macho >= 1 && d.qualidade_semen_macho <= 5 &&
    typeof d.filhos_nascidos_macho === 'number' && isFinite(d.filhos_nascidos_macho) && d.filhos_nascidos_macho >= 0 && d.filhos_nascidos_macho <= 200 &&
    typeof d.parentesco_endogamia === 'number' && isFinite(d.parentesco_endogamia) && d.parentesco_endogamia >= 0 && d.parentesco_endogamia <= 1
  )
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
  const idadeMin = d.especie === 'Bovino' ? 3 : 2
  const idadeMax = d.especie === 'Bovino' ? 8 : 6

  const eccScore          = scoreRange(d.ecc_matriz, 2.5, 4.0) * 0.14
  const idadeMatrizScore  = scoreRange(d.idade_matriz, idadeMin, idadeMax) * 0.10
  const diasScore         = d.dias_desde_ultimo_parto > 0
    ? scoreRange(d.dias_desde_ultimo_parto, 60, 180) * 0.12
    : 0.06
  const partosScore       = Math.min(d.numero_partos_matriz / 5, 1) * 0.08
  const abortosScore      = Math.max(0, 1 - d.abortos_matriz / 3) * 0.10
  const filhosMatrizScore = Math.min(d.filhos_nascidos_matriz / 8, 1) * 0.08
  const pesoTarget        = d.especie === 'Bovino' ? 420 : d.especie === 'Caprino' ? 48 : 52
  const pesoMatrizScore   = Math.max(0, scoreRange(d.peso_matriz_kg, pesoTarget * 0.75, pesoTarget * 1.3)) * 0.08
  const racaMatrizScore   = racaBonus('geral', d.raca_matriz) * 0.08

  const semenScore        = (d.qualidade_semen_macho / 5) * 0.16
  const idadeMachoMin     = d.especie === 'Bovino' ? 2.5 : 1.5
  const idadeMachoMax     = d.especie === 'Bovino' ? 9 : 7
  const idadeMachoScore   = scoreRange(d.idade_macho, idadeMachoMin, idadeMachoMax) * 0.06
  const filhosMachoScore  = Math.min(d.filhos_nascidos_macho / 30, 1) * 0.06
  const racaMachoScore    = racaBonus('geral', d.raca_macho) * 0.06

  const endogamiaScore    = (1 - Math.min(d.parentesco_endogamia * 4, 1)) * 0.08

  const raw =
    eccScore + idadeMatrizScore + diasScore + partosScore + abortosScore +
    filhosMatrizScore + pesoMatrizScore + racaMatrizScore +
    semenScore + idadeMachoScore + filhosMachoScore + racaMachoScore +
    endogamiaScore

  const probability = Math.min(Math.max(raw * 1.15, 0.08), 0.96)
  return { probability, predictedClass: probability >= 0.5 ? 1 : 0 }
}

export async function POST(req: NextRequest) {
  try {
    const body = await req.json()
    if (!validateInput(body)) {
      return NextResponse.json({ error: 'Dados inválidos ou fora do intervalo permitido.' }, { status: 400 })
    }
    const result = predictPrenhez(body)
    return NextResponse.json({
      probability: result.probability,
      predictedClass: result.predictedClass,
      percentual: Math.round(result.probability * 100),
    })
  } catch {
    return NextResponse.json({ error: 'Dados inválidos.' }, { status: 400 })
  }
}
