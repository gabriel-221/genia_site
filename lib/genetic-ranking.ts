import type { Animal, ScoredAnimal } from '@/types'

export function rankAnimals(objetivo: string, animais: Animal[]): ScoredAnimal[] {
  return animais
    .map(a => calcularScore(objetivo.toLowerCase(), a))
    .sort((a, b) => b.scoreFinal - a.scoreFinal)
}

function calcularScore(objetivo: string, animal: Animal): ScoredAnimal {
  const age = calcularIdade(animal.data_nascimento)
  const reproBase = animal.sexo === 'femea'
    ? calcularBaseFemea(animal, age)
    : calcularBaseMacho(animal, age)

  const prolificidade = animal.sexo === 'femea'
    ? Math.min(animal.filhos_matriz / 12, 1)
    : Math.min(animal.filhos_macho / 40, 1)

  const breedBonus = calcularBonusRaca(objetivo, animal.raca)

  let baseScore: number
  if (objetivo === 'leite') {
    const fertilityPenalty = Math.max(1 - (animal.abortos / 4), 0.4)
    const leiteScore = Math.min((animal.producao_leite_diaria || 0) / 30, 1)
    const leiteBonus = (animal.producao_leite_diaria || 0) > 0 ? leiteScore * 0.20 : 0
    baseScore = reproBase * 0.26 + ((animal.escore_corporal || 3) / 5) * 0.20 +
      prolificidade * 0.16 + fertilityPenalty * 0.12 +
      Math.min((animal.peso_kg || 0) / 700, 1) * 0.06 + leiteBonus
  } else if (objetivo === 'corte') {
    const weightDiv = animal.sexo === 'macho' ? 1100 : 700
    const semenScore = animal.sexo === 'macho' ? animal.qualidade_semen / 5 : 0.6
    baseScore = Math.min((animal.peso_kg || 0) / weightDiv, 1) * 0.34 +
      reproBase * 0.24 + semenScore * 0.16 + prolificidade * 0.14 +
      ((animal.escore_corporal || 3) / 5) * 0.12
  } else {
    const fertilityPenalty = Math.max(1 - (animal.abortos / 4), 0.4)
    const semenScore = animal.sexo === 'macho' ? animal.qualidade_semen / 5 : 0.6
    baseScore = reproBase * 0.42 + prolificidade * 0.20 + semenScore * 0.18 +
      fertilityPenalty * 0.10 + ((animal.escore_corporal || 3) / 5) * 0.10
  }

  const finalValue = Math.min((baseScore + breedBonus) * 100, 100)

  return {
    animal,
    scoreFinal: finalValue,
    classificacao:
      finalValue >= 82 ? 'Elite genética' :
      finalValue >= 68 ? 'Alto potencial' :
      finalValue >= 54 ? 'Bom desempenho' : 'Monitorar',
    fatores: identificarFatores(animal, objetivo, breedBonus > 0),
  }
}

function calcularBaseFemea(a: Animal, age: number): number {
  const ecc = ((a.escore_corporal || 3) / 5) * 0.24
  const prolif = Math.min(a.filhos_matriz / 12, 1) * 0.18
  const endogamia = (1 - Math.min((a.coef_endogamia || 0) * 1.5, 0.5)) * 0.16
  const abortos = (1 - Math.min(a.abortos / 4, 0.6)) * 0.14
  const posParto = scoreRange(a.dias_ultimo_parto > 0 ? a.dias_ultimo_parto : 120, 70, 160) * 0.14
  const idealAge = scoreRange(age, 3, 7) * 0.08
  const peso = Math.min((a.peso_kg || 0) / 700, 1) * 0.06
  return ecc + prolif + endogamia + abortos + posParto + idealAge + peso
}

function calcularBaseMacho(a: Animal, age: number): number {
  const semen = (a.qualidade_semen / 5) * 0.34
  const prolif = Math.min(a.filhos_macho / 40, 1) * 0.24
  const endogamia = (1 - Math.min((a.coef_endogamia || 0) * 1.5, 0.5)) * 0.18
  const idealAge = scoreRange(age, 3, 8) * 0.14
  const peso = Math.min((a.peso_kg || 0) / 1100, 1) * 0.10
  return semen + prolif + endogamia + idealAge + peso
}

function scoreRange(valor: number, min: number, max: number): number {
  const centro = (min + max) / 2
  const range = (max - min) / 2
  if (range === 0) return 1
  return Math.max(0, Math.min(1, 1 - Math.abs(valor - centro) / range))
}

function calcularBonusRaca(objetivo: string, raca: string): number {
  const r = raca.toLowerCase()
  const map: Record<string, string[]> = {
    leite: ['girolando', 'holandes', 'gir', 'saanen', 'toggenburg'],
    corte: ['angus', 'nelore', 'boer', 'dorper', 'texel', 'morada nova'],
    fertilidade: ['gir', 'girolando', 'santa ines', 'moxoto', 'anglo nubiana'],
  }
  return (map[objetivo] ?? []).some(b => r.includes(b)) ? 0.18 : 0
}

function identificarFatores(a: Animal, objetivo: string, temBonusRaca: boolean): string[] {
  const list: string[] = []
  if (temBonusRaca) list.push(`Aptidão racial para ${objetivo}`)
  if ((a.escore_corporal || 0) >= 3.5) list.push('Excelente escore corporal')
  if ((a.coef_endogamia || 0) < 0.05) list.push('Baixa endogamia')
  if (a.sexo === 'macho' && a.qualidade_semen >= 4) list.push('Alta qualidade seminal')
  if (a.sexo === 'femea' && a.abortos === 0) list.push('Histórico reprodutivo estável')
  if ((a.producao_leite_diaria || 0) >= 15) list.push(`Produção de leite: ${a.producao_leite_diaria}L/dia`)
  return list.slice(0, 3)
}

function calcularIdade(dataNascimento?: string): number {
  if (!dataNascimento) return 3
  const nascimento = new Date(dataNascimento)
  const hoje = new Date()
  return (hoje.getTime() - nascimento.getTime()) / (365.25 * 24 * 60 * 60 * 1000)
}
