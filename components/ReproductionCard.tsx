'use client'
import type { EventoReprodutivo, EventoTipo } from '@/types'
import { formatDate } from '@/lib/utils'

const eventoLabel: Record<EventoTipo, string> = {
  cio: 'Detecção de Cio',
  inseminacao: 'Inseminação Artificial',
  diagnostico: 'Diagnóstico de Gestação',
  parto: 'Parto',
  desmame: 'Desmame',
}

interface Props {
  lastEvento: EventoReprodutivo | null
  taxaPrenhez: number | null
}

export default function ReproductionCard({ lastEvento, taxaPrenhez }: Props) {
  return (
    <div className="bg-white rounded-3xl shadow-sm p-5">
      <h2 className="text-xs font-black text-gray-400 uppercase tracking-widest mb-4">
        Reprodução
      </h2>

      {taxaPrenhez !== null && (
        <div className="mb-5">
          <div className="flex justify-between items-baseline mb-1.5">
            <span className="text-sm text-gray-500 font-medium">Taxa de Prenhez</span>
            <span className="text-2xl font-black text-emerald-600 tabular-nums">
              {taxaPrenhez}%
            </span>
          </div>
          <div className="h-3 bg-gray-100 rounded-full overflow-hidden">
            <div
              className="h-full rounded-full bg-gradient-to-r from-emerald-400 to-emerald-600 transition-all duration-700"
              style={{ width: `${taxaPrenhez}%` }}
            />
          </div>
        </div>
      )}

      {lastEvento && (
        <div className="space-y-0 divide-y divide-gray-50">
          <div className="flex justify-between py-2.5">
            <span className="text-xs text-gray-400">Último evento</span>
            <span className="text-xs font-semibold text-gray-700">
              {eventoLabel[lastEvento.tipo] ?? lastEvento.tipo}
            </span>
          </div>
          <div className="flex justify-between py-2.5">
            <span className="text-xs text-gray-400">Data</span>
            <span className="text-xs font-semibold text-gray-700">
              {formatDate(lastEvento.data_evento)}
            </span>
          </div>
          {lastEvento.gestacao_confirmada != null && (
            <div className="flex justify-between py-2.5">
              <span className="text-xs text-gray-400">Gestação</span>
              {lastEvento.gestacao_confirmada ? (
                <span className="text-xs font-semibold text-emerald-600">✓ Confirmada</span>
              ) : (
                <span className="text-xs font-semibold text-gray-400">Não confirmada</span>
              )}
            </div>
          )}
          {lastEvento.data_parto_previsto && (
            <div className="flex justify-between py-2.5">
              <span className="text-xs text-gray-400">Parto previsto</span>
              <span className="text-xs font-semibold text-amber-600">
                📅 {formatDate(lastEvento.data_parto_previsto)}
              </span>
            </div>
          )}
          {lastEvento.tecnico_responsavel && (
            <div className="flex justify-between py-2.5">
              <span className="text-xs text-gray-400">Técnico</span>
              <span className="text-xs font-semibold text-gray-700">
                {lastEvento.tecnico_responsavel}
              </span>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
