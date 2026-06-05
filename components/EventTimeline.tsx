import type { EventoReprodutivo, EventoTipo } from '@/types'
import { formatDate } from '@/lib/utils'

const TIPO_CONFIG: Record<EventoTipo, { label: string; color: string; bg: string; icon: string }> = {
  cio:         { label: 'Detecção de Cio',       color: '#10B981', bg: '#D1FAE5', icon: '🌿' },
  inseminacao: { label: 'Inseminação Artificial', color: '#3B82F6', bg: '#DBEAFE', icon: '💉' },
  diagnostico: { label: 'Diagnóstico',            color: '#8B5CF6', bg: '#EDE9FE', icon: '🔬' },
  parto:       { label: 'Parto',                  color: '#F97316', bg: '#FFEDD5', icon: '🐣' },
  desmame:     { label: 'Desmame',                color: '#EC4899', bg: '#FCE7F3', icon: '🌱' },
}

interface Props {
  eventos: EventoReprodutivo[]
}

export default function EventTimeline({ eventos }: Props) {
  if (!eventos.length) return null

  return (
    <div className="bg-white rounded-3xl shadow-sm p-5">
      <h2 className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-4">
        Histórico Reprodutivo
      </h2>

      <div className="relative">
        {/* Vertical line */}
        <div className="absolute left-[18px] top-2 bottom-2 w-px bg-gray-100" />

        <div className="space-y-4">
          {eventos.map((ev, i) => {
            const cfg = TIPO_CONFIG[ev.tipo] ?? { label: ev.tipo, color: '#6B7280', bg: '#F3F4F6', icon: '📋' }
            const isFirst = i === 0
            return (
              <div key={ev.id} className="flex gap-3 relative">
                {/* Timeline dot */}
                <div
                  className="relative z-10 w-9 h-9 rounded-full flex items-center justify-center shrink-0 text-base shadow-sm"
                  style={{ background: cfg.bg, border: `2px solid ${cfg.color}33` }}
                >
                  {cfg.icon}
                  {isFirst && (
                    <span className="absolute -top-1 -right-1 w-3 h-3 rounded-full bg-green-500 border-2 border-white" />
                  )}
                </div>

                {/* Content */}
                <div className="flex-1 bg-gray-50 rounded-2xl px-4 py-3 min-w-0">
                  <div className="flex items-start justify-between gap-2">
                    <p className="text-sm font-bold text-gray-900 leading-tight">{cfg.label}</p>
                    <span className="text-[10px] font-semibold text-gray-400 whitespace-nowrap shrink-0 mt-0.5">
                      {formatDate(ev.data_evento)}
                    </span>
                  </div>

                  {ev.gestacao_confirmada != null && (
                    <p className="text-xs mt-1" style={{ color: cfg.color }}>
                      {ev.gestacao_confirmada ? '✓ Gestação confirmada' : '✗ Gestação não confirmada'}
                    </p>
                  )}
                  {ev.data_parto_previsto && (
                    <p className="text-xs mt-1 text-amber-600 font-medium">
                      📅 Parto previsto: {formatDate(ev.data_parto_previsto)}
                    </p>
                  )}
                  {ev.score_ia_prenhez != null && (
                    <p className="text-xs mt-1 text-purple-600 font-medium">
                      🤖 IA: {Math.round(ev.score_ia_prenhez * 100)}% de chance de prenhez
                    </p>
                  )}
                  {ev.tecnico_responsavel && (
                    <p className="text-xs mt-1 text-gray-500">👤 {ev.tecnico_responsavel}</p>
                  )}
                  {ev.observacoes && (
                    <p className="text-xs mt-1 text-gray-500 italic">&ldquo;{ev.observacoes}&rdquo;</p>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      </div>

      {eventos.length >= 5 && (
        <p className="text-center text-[10px] text-gray-300 mt-4">
          Exibindo os 5 eventos mais recentes
        </p>
      )}
    </div>
  )
}
