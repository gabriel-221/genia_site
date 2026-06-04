'use client'
import type { Animal } from '@/types'
import { formatDate, calcAge } from '@/lib/utils'

interface StatBarProps {
  label: string
  value: number
  max: number
  unit?: string
  color?: string
}

function StatBar({ label, value, max, unit, color = '#2D6A4F' }: StatBarProps) {
  const pct = Math.min((value / max) * 100, 100)
  return (
    <div className="mb-4">
      <div className="flex justify-between items-baseline mb-1.5">
        <span className="text-sm text-gray-500 font-medium">{label}</span>
        <span className="text-sm font-black text-gray-800 tabular-nums">
          {value}
          {unit ? ` ${unit}` : ''}
        </span>
      </div>
      <div className="h-2.5 bg-gray-100 rounded-full overflow-hidden">
        <div
          className="h-full rounded-full transition-all duration-700"
          style={{ backgroundColor: color, width: `${pct}%` }}
        />
      </div>
    </div>
  )
}

export default function StatsCard({ animal }: { animal: Animal }) {
  const hasStats = animal.peso_kg || animal.escore_corporal || animal.coef_endogamia != null

  return (
    <div className="bg-white rounded-3xl shadow-sm p-5">
      <h2 className="text-xs font-black text-gray-400 uppercase tracking-widest mb-4">
        Estatísticas
      </h2>

      {hasStats ? (
        <>
          {animal.peso_kg != null && (
            <StatBar label="Peso" value={animal.peso_kg} max={600} unit="kg" color="#D97706" />
          )}
          {animal.escore_corporal != null && (
            <StatBar label="Escore Corporal" value={animal.escore_corporal} max={5} color="#2D6A4F" />
          )}
          {animal.coef_endogamia != null && (
            <StatBar
              label="Coef. de Endogamia"
              value={parseFloat((animal.coef_endogamia * 100).toFixed(1))}
              max={25}
              unit="%"
              color="#EF4444"
            />
          )}
        </>
      ) : (
        <p className="text-sm text-gray-400 text-center py-1">Métricas não registradas</p>
      )}

      <div className="mt-3 pt-4 border-t border-gray-50 grid grid-cols-2 gap-y-3 gap-x-4">
        {animal.data_nascimento && (
          <div>
            <p className="text-xs text-gray-400 mb-0.5">Nascimento</p>
            <p className="text-sm font-semibold text-gray-700">
              {formatDate(animal.data_nascimento)}
            </p>
            <p className="text-xs text-gray-400">{calcAge(animal.data_nascimento)}</p>
          </div>
        )}
        {animal.linhagem && (
          <div>
            <p className="text-xs text-gray-400 mb-0.5">Linhagem</p>
            <p className="text-sm font-semibold text-gray-700 truncate">{animal.linhagem}</p>
          </div>
        )}
        {animal.raca && (
          <div>
            <p className="text-xs text-gray-400 mb-0.5">Raça</p>
            <p className="text-sm font-semibold text-gray-700">{animal.raca}</p>
          </div>
        )}
        {animal.rfid_tag && (
          <div>
            <p className="text-xs text-gray-400 mb-0.5">Tag NFC</p>
            <p className="text-sm font-mono font-semibold text-gray-700 truncate">
              {animal.rfid_tag}
            </p>
          </div>
        )}
      </div>
    </div>
  )
}
