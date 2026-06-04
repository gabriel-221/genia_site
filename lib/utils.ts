import type { Especie } from '@/types'

export function especieEmoji(especie: Especie): string {
  const map = { bovino: '🐄', ovino: '🐑', caprino: '🐐' }
  return map[especie] ?? '🐄'
}

export function especieLabel(especie: Especie): string {
  const map = { bovino: 'Bovino', ovino: 'Ovino', caprino: 'Caprino' }
  return map[especie] ?? especie
}

export function getSpeciesGradient(especie: Especie): { from: string; to: string } {
  const map = {
    bovino:  { from: '#D97706', to: '#78350F' },
    ovino:   { from: '#0EA5E9', to: '#1E40AF' },
    caprino: { from: '#10B981', to: '#064E3B' },
  }
  return map[especie] ?? { from: '#2D6A4F', to: '#1B4332' }
}

export function getSpeciesAccent(especie: Especie): string {
  const map = { bovino: '#D97706', ovino: '#0EA5E9', caprino: '#10B981' }
  return map[especie] ?? '#2D6A4F'
}

export function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

export function calcAge(dateStr: string): string {
  const birth = new Date(dateStr)
  const now = new Date()
  const totalMonths =
    (now.getFullYear() - birth.getFullYear()) * 12 +
    (now.getMonth() - birth.getMonth())
  if (totalMonths < 24) return `${totalMonths} meses`
  const years = Math.floor(totalMonths / 12)
  return `${years} ${years === 1 ? 'ano' : 'anos'}`
}
