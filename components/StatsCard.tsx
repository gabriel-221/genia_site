'use client'
import type { Animal } from '@/types'
import { formatDate, calcAge } from '@/lib/utils'

interface StatBarProps {
  label: string
  value: number
  max: number
  unit?: string
  color: string
  glow: string
}

function StatBar({ label, value, max, unit, color, glow }: StatBarProps) {
  const pct = Math.min((value / max) * 100, 100)
  return (
    <div className="mb-3">
      <div className="flex justify-between items-baseline mb-1">
        <span className="text-xs text-gray-500 font-semibold">{label}</span>
        <span className="text-sm font-black text-gray-800 tabular-nums">
          {value}{unit ? ` ${unit}` : ''}
        </span>
      </div>
      <div className="h-2.5 bg-gray-100 rounded-full overflow-hidden">
        <div
          className="h-full rounded-full transition-all duration-700"
          style={{
            width: `${pct}%`,
            background: `linear-gradient(90deg, ${color}, ${glow})`,
            boxShadow: `0 0 6px ${glow}55`,
          }}
        />
      </div>
    </div>
  )
}

export default function StatsCard({ animal }: { animal: Animal }) {
  const hasStats = animal.peso_kg || animal.escore_corporal

  // Genetic potential score (0-100) derived from reproductive data
  let geneticScore: number | null = null
  if (animal.sexo === 'femea') {
    const ecc   = ((animal.escore_corporal ?? 3) / 5) * 30
    const hist  = Math.min(animal.filhos_matriz / 8, 1) * 20
    const abort = Math.max(0, 1 - animal.abortos / 3) * 20
    const leite = animal.producao_leite_diaria > 0 ? Math.min(animal.producao_leite_diaria / 20, 1) * 30 : 15
    geneticScore = Math.round(ecc + hist + abort + leite)
  } else {
    const semen = ((animal.qualidade_semen ?? 3) / 5) * 40
    const filhos = Math.min(animal.filhos_macho / 30, 1) * 40
    const ecc   = ((animal.escore_corporal ?? 3) / 5) * 20
    geneticScore = Math.round(semen + filhos + ecc)
  }

  const scoreColor = geneticScore < 40 ? '#EF4444' : geneticScore < 65 ? '#F59E0B' : '#10B981'
  const scoreLabel = geneticScore < 40 ? 'Desenvolvimento' : geneticScore < 65 ? 'Bom potencial' : 'Elite genética'

  return (
    <div className="bg-white rounded-3xl shadow-sm overflow-hidden">
      {/* Genetic score banner */}
      <div className="px-5 pt-4 pb-3 border-b border-gray-50">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest">
              Potencial Genético
            </p>
            <p className="text-2xl font-black tabular-nums" style={{ color: scoreColor }}>
              {geneticScore}<span className="text-base text-gray-300 font-normal">/100</span>
            </p>
          </div>
          <span className="text-xs font-bold px-3 py-1.5 rounded-full"
            style={{ background: `${scoreColor}18`, color: scoreColor }}>
            {scoreLabel}
          </span>
        </div>
        <div className="h-2 bg-gray-100 rounded-full mt-2 overflow-hidden">
          <div className="h-full rounded-full transition-all duration-700"
            style={{
              width: `${geneticScore}%`,
              background: `linear-gradient(90deg, ${scoreColor}99, ${scoreColor})`,
            }} />
        </div>
      </div>

      <div className="p-5">
        <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-4">
          Estatísticas
        </p>

        {hasStats ? (
          <>
            {animal.peso_kg != null && (
              <StatBar label="Peso corporal" value={animal.peso_kg}
                max={animal.especie === 'bovino' ? 700 : animal.especie === 'caprino' ? 80 : 100}
                unit="kg" color="#D97706" glow="#FCD34D" />
            )}
            {animal.escore_corporal != null && (
              <StatBar label="Escore Corporal (ECC)" value={animal.escore_corporal}
                max={5} color="#2D6A4F" glow="#34D399" />
            )}
            {animal.coef_endogamia != null && animal.coef_endogamia > 0 && (
              <StatBar
                label="Coef. Endogamia"
                value={parseFloat((animal.coef_endogamia * 100).toFixed(1))}
                max={25} unit="%" color="#EF4444" glow="#FCA5A5" />
            )}
            {animal.sexo === 'femea' && (animal.producao_leite_diaria ?? 0) > 0 && (
              <StatBar label="Produção de leite" value={animal.producao_leite_diaria}
                max={30} unit="L/dia" color="#0EA5E9" glow="#7DD3FC" />
            )}
            {animal.sexo === 'macho' && (
              <StatBar label="Qualidade seminal" value={animal.qualidade_semen ?? 3}
                max={5} color="#8B5CF6" glow="#C4B5FD" />
            )}
          </>
        ) : (
          <p className="text-sm text-gray-400 text-center py-2">Métricas não registradas</p>
        )}

        {/* Info grid */}
        <div className="mt-4 pt-4 border-t border-gray-50 grid grid-cols-2 gap-y-3 gap-x-4">
          {animal.data_nascimento && (
            <div>
              <p className="text-[10px] text-gray-400 mb-0.5 uppercase font-semibold tracking-wide">Nascimento</p>
              <p className="text-sm font-semibold text-gray-700">{formatDate(animal.data_nascimento)}</p>
              <p className="text-xs text-gray-400">{calcAge(animal.data_nascimento)}</p>
            </div>
          )}
          {animal.linhagem && (
            <div>
              <p className="text-[10px] text-gray-400 mb-0.5 uppercase font-semibold tracking-wide">Linhagem</p>
              <p className="text-sm font-semibold text-gray-700 truncate">{animal.linhagem}</p>
            </div>
          )}
          {animal.sexo === 'femea' && animal.numero_partos > 0 && (
            <div>
              <p className="text-[10px] text-gray-400 mb-0.5 uppercase font-semibold tracking-wide">Partos</p>
              <p className="text-sm font-semibold text-gray-700">{animal.numero_partos}</p>
            </div>
          )}
          {animal.sexo === 'femea' && animal.filhos_matriz > 0 && (
            <div>
              <p className="text-[10px] text-gray-400 mb-0.5 uppercase font-semibold tracking-wide">Filhos</p>
              <p className="text-sm font-semibold text-gray-700">{animal.filhos_matriz}</p>
            </div>
          )}
          {animal.sexo === 'macho' && animal.filhos_macho > 0 && (
            <div>
              <p className="text-[10px] text-gray-400 mb-0.5 uppercase font-semibold tracking-wide">Prole</p>
              <p className="text-sm font-semibold text-gray-700">{animal.filhos_macho} filhos</p>
            </div>
          )}
          {animal.rfid_tag && (
            <div>
              <p className="text-[10px] text-gray-400 mb-0.5 uppercase font-semibold tracking-wide">Tag NFC</p>
              <p className="text-xs font-mono font-semibold text-gray-700 truncate">{animal.rfid_tag}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
