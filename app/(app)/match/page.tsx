'use client'
import { useEffect, useState } from 'react'
import { createClient } from '@/lib/supabase/client'
import type { Animal, ScoredAnimal } from '@/types'
import { ESPECIE_EMOJI, ESPECIE_LABEL } from '@/types'
import { rankAnimals } from '@/lib/genetic-ranking'
import { Dna, ChevronRight, ArrowLeft, CheckCircle2 } from '@/components/Icons'

const OBJETIVOS = [
  { key: 'leite',       label: 'Leite',       emoji: '🥛', desc: 'Maior produção leiteira' },
  { key: 'corte',       label: 'Corte',       emoji: '🥩', desc: 'Maior ganho de peso' },
  { key: 'fertilidade', label: 'Fertilidade', emoji: '🌱', desc: 'Melhor taxa reprodutiva' },
]

function classifCor(c: string) {
  if (c === 'Elite genética')  return 'bg-blue-100 text-blue-700'
  if (c === 'Alto potencial')  return 'bg-green-100 text-green-700'
  if (c === 'Bom desempenho') return 'bg-amber-100 text-amber-700'
  return 'bg-gray-100 text-gray-600'
}

export default function MatchPage() {
  const supabase = createClient()
  const [animais, setAnimais] = useState<Animal[]>([])
  const [objetivo, setObjetivo] = useState('')
  const [ranking, setRanking] = useState<ScoredAnimal[]>([])
  const [loading, setLoading] = useState(false)
  const [step, setStep] = useState<'select' | 'result'>('select')

  useEffect(() => {
    async function load() {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) return
      const { data: prod } = await supabase.from('genia_produtor').select('id').eq('user_id', user.id).single()
      if (!prod) return
      const { data } = await supabase.from('genia_animal').select('*').eq('produtor_id', prod.id)
      setAnimais(data ?? [])
    }
    load()
  }, [])

  function buscar() {
    if (!objetivo) return
    setLoading(true)
    const result = rankAnimals(objetivo, animais)
    setRanking(result)
    setLoading(false)
    setStep('result')
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white px-5 py-4 border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-lg mx-auto flex items-center gap-3">
          {step === 'result' && (
            <button onClick={() => { setStep('select'); setObjetivo('') }}>
              <ArrowLeft className="w-5 h-5 text-gray-600" />
            </button>
          )}
          <div>
            <div className="flex items-center gap-2">
              <Dna className="w-5 h-5 text-green-700" />
              <h1 className="text-xl font-extrabold text-green-800">GeneMatch</h1>
            </div>
            <p className="text-xs text-gray-500">
              {step === 'select' ? 'Ranqueamento genético por objetivo' : `Ranking: ${OBJETIVOS.find(o => o.key === objetivo)?.label}`}
            </p>
          </div>
        </div>
      </div>

      <div className="max-w-lg mx-auto px-4 py-5">
        {step === 'select' && (
          <div className="space-y-4">
            <h2 className="text-lg font-bold text-gray-900">Qual é o seu objetivo genético?</h2>

            <div className="grid gap-3">
              {OBJETIVOS.map(o => (
                <button key={o.key} onClick={() => setObjetivo(o.key)}
                  className={`w-full p-4 rounded-2xl border-2 text-left transition ${
                    objetivo === o.key
                      ? 'border-green-800 bg-green-50'
                      : 'border-gray-200 bg-white'
                  }`}>
                  <div className="flex items-center gap-3">
                    <span className="text-3xl">{o.emoji}</span>
                    <div>
                      <p className={`font-bold ${objetivo === o.key ? 'text-green-800' : 'text-gray-900'}`}>{o.label}</p>
                      <p className="text-xs text-gray-500">{o.desc}</p>
                    </div>
                    {objetivo === o.key && <CheckCircle2 className="w-5 h-5 text-green-800 ml-auto" />}
                  </div>
                </button>
              ))}
            </div>

            {/* Info card */}
            <div className="bg-green-50 rounded-2xl p-4 flex gap-3">
              <span className="text-xl shrink-0">💡</span>
              <p className="text-xs text-green-800 leading-relaxed">
                O GENIA analisa seu plantel e ranqueia os animais com base em critérios técnicos
                de cada objetivo genético usando nosso algoritmo proprietário.
              </p>
            </div>

            <button
              onClick={buscar}
              disabled={!objetivo || animais.length === 0}
              className="w-full py-3.5 bg-green-800 text-white font-bold rounded-xl disabled:opacity-50 transition hover:bg-green-700"
            >
              {animais.length === 0 ? 'Nenhum animal cadastrado' : 'RANQUEAR ANIMAIS'}
            </button>
          </div>
        )}

        {step === 'result' && (
          <div className="space-y-3">
            {ranking.length === 0 ? (
              <div className="text-center py-16 text-gray-500">Nenhum animal para ranquear.</div>
            ) : ranking.map((item, idx) => (
              <div key={item.animal.id} className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm flex items-start gap-3">
                {/* Posição */}
                <div className={`w-9 h-9 rounded-full flex items-center justify-center font-extrabold text-sm shrink-0 ${
                  idx === 0 ? 'bg-yellow-400 text-yellow-900' :
                  idx === 1 ? 'bg-gray-200 text-gray-600' :
                  idx === 2 ? 'bg-orange-200 text-orange-700' :
                  'bg-gray-100 text-gray-500'
                }`}>
                  {idx + 1}
                </div>

                <div className="flex-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    <p className="font-bold text-gray-900">{item.animal.nome}</p>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${classifCor(item.classificacao)}`}>
                      {item.classificacao}
                    </span>
                  </div>
                  <p className="text-xs text-gray-500 mt-0.5">
                    {ESPECIE_EMOJI[item.animal.especie]} {item.animal.raca} · {ESPECIE_LABEL[item.animal.especie]}
                  </p>
                  <div className="mt-2 space-y-0.5">
                    {item.fatores.map((f, i) => (
                      <p key={i} className="text-[11px] text-green-700 flex items-center gap-1">
                        <CheckCircle2 className="w-3 h-3" /> {f}
                      </p>
                    ))}
                  </div>
                </div>

                <div className="text-right shrink-0">
                  <p className="text-[10px] text-gray-400">Score</p>
                  <p className="text-xl font-extrabold text-green-800">{item.scoreFinal.toFixed(1)}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
