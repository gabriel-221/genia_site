'use client'
import { useEffect, useState } from 'react'
import { createClient } from '@/lib/supabase/client'
import type { Animal } from '@/types'
import { ESPECIE_EMOJI } from '@/types'
import { ChevronLeft, ChevronRight, Plus, Heart, Syringe, Baby, Microscope, X } from '@/components/Icons'

const MESES_PT = ['Janeiro','Fevereiro','Março','Abril','Maio','Junho',
                  'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro']
const DIAS_PT = ['Dom','Seg','Ter','Qua','Qui','Sex','Sáb']

type TipoEvento = 'cio' | 'inseminacao' | 'diagnostico' | 'parto' | 'desmame'

function eventoIcon(tipo: TipoEvento) {
  const cls = 'w-3.5 h-3.5'
  if (tipo === 'cio') return <div className={`${cls} rounded-full bg-green-500`} />
  if (tipo === 'inseminacao') return <Syringe className={`${cls} text-blue-600`} />
  if (tipo === 'diagnostico') return <Microscope className={`${cls} text-purple-600`} />
  if (tipo === 'parto') return <Baby className={`${cls} text-orange-500`} />
  return <Heart className={`${cls} text-pink-500`} />
}

function eventoCor(tipo: TipoEvento) {
  if (tipo === 'cio') return 'bg-green-500'
  if (tipo === 'inseminacao') return 'bg-blue-500'
  if (tipo === 'diagnostico') return 'bg-purple-500'
  if (tipo === 'parto') return 'bg-orange-500'
  return 'bg-pink-500'
}

export default function CalendarioPage() {
  const supabase = createClient()
  const hoje = new Date()
  const [mes, setMes] = useState(hoje.getMonth())
  const [ano, setAno] = useState(hoje.getFullYear())
  const [diaSel, setDiaSel] = useState<number | null>(null)
  const [animais, setAnimais] = useState<Animal[]>([])
  const [showDialog, setShowDialog] = useState(false)
  const [saving, setSaving] = useState(false)
  const [formAnimal, setFormAnimal] = useState('')
  const [formTipo, setFormTipo] = useState<TipoEvento>('cio')
  const [formObs, setFormObs] = useState('')
  const [produtorId, setProdutorId] = useState<string | null>(null)
  const [eventos, setEventos] = useState<Record<string, TipoEvento>>({})

  useEffect(() => {
    async function load() {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) return
      const { data: prod } = await supabase.from('genia_produtor').select('id').eq('user_id', user.id).single()
      if (!prod) return
      setProdutorId(prod.id)
      const { data: anim } = await supabase.from('genia_animal').select('*').eq('produtor_id', prod.id)
      setAnimais(anim ?? [])

      // Gera marcadores dinâmicos baseados nos animais
      const map: Record<string, TipoEvento> = {}
      const todayStr = `${hoje.getFullYear()}-${String(hoje.getMonth()+1).padStart(2,'0')}-${String(hoje.getDate()).padStart(2,'0')}`
      ;(anim ?? []).forEach((a: Animal, i: number) => {
        if (a.prenhou) {
          const d = new Date(hoje); d.setDate(d.getDate() + (i % 20))
          map[d.toISOString().slice(0,10)] = 'parto'
        } else if (a.sexo === 'femea') {
          const d = new Date(hoje); d.setDate(d.getDate() + (i % 10))
          map[d.toISOString().slice(0,10)] = 'cio'
        }
      })
      if ((anim?.length ?? 0) > 0) map[todayStr] = 'inseminacao'
      setEventos(map)
    }
    load()
  }, [])

  async function salvarEvento() {
    if (!formAnimal || !produtorId) return
    const animal = animais.find(a => a.id === formAnimal)
    if (!animal) return
    setSaving(true)
    const dataEvento = diaSel
      ? `${ano}-${String(mes+1).padStart(2,'0')}-${String(diaSel).padStart(2,'0')}`
      : new Date().toISOString().slice(0,10)

    await supabase.from('genia_evento_reprodutivo').insert({
      animal_id: animal.id,
      tipo: formTipo,
      data_evento: dataEvento,
      semen_reprodutor: '',
      observacoes: formObs,
    })
    setSaving(false)
    setShowDialog(false)
    setFormObs('')
    // Atualiza marcador
    setEventos(prev => ({ ...prev, [dataEvento]: formTipo }))
  }

  // Grid do calendário
  const primeiroDia = new Date(ano, mes, 1).getDay()
  const diasNoMes = new Date(ano, mes + 1, 0).getDate()

  function prevMes() {
    if (mes === 0) { setMes(11); setAno(a => a - 1) } else setMes(m => m - 1)
  }
  function nextMes() {
    if (mes === 11) { setMes(0); setAno(a => a + 1) } else setMes(m => m + 1)
  }

  const alertas = animais.slice(0, 5).map((a, i) => ({
    nome: a.nome,
    desc: a.prenhou ? 'Gestação confirmada' : a.sexo === 'femea' ? 'Cio previsto' : 'Disponível para reprodução',
    tipo: (a.prenhou ? 'parto' : a.sexo === 'femea' ? 'cio' : 'inseminacao') as TipoEvento,
    emoji: ESPECIE_EMOJI[a.especie],
  }))

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white px-5 py-4 border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-lg mx-auto flex items-center justify-between">
          <h1 className="text-xl font-bold text-gray-900">Calendário</h1>
          <button onClick={() => setShowDialog(true)}
            className="bg-green-800 text-white text-xs font-bold px-4 py-2 rounded-xl flex items-center gap-1">
            <Plus className="w-4 h-4" /> Registrar
          </button>
        </div>
      </div>

      <div className="max-w-lg mx-auto px-4 py-4 space-y-4">
        {/* Calendário */}
        <div className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm">
          {/* Nav mês */}
          <div className="flex items-center justify-between mb-4">
            <button onClick={prevMes} className="p-1 text-gray-500">
              <ChevronLeft className="w-5 h-5" />
            </button>
            <p className="font-bold text-gray-900">{MESES_PT[mes]} {ano}</p>
            <button onClick={nextMes} className="p-1 text-gray-500">
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>

          {/* Dias semana */}
          <div className="grid grid-cols-7 mb-2">
            {DIAS_PT.map(d => (
              <div key={d} className="text-center text-[10px] text-gray-400 font-medium">{d}</div>
            ))}
          </div>

          {/* Grid */}
          <div className="grid grid-cols-7 gap-y-1">
            {Array(primeiroDia).fill(null).map((_, i) => <div key={`e${i}`} />)}
            {Array(diasNoMes).fill(null).map((_, i) => {
              const dia = i + 1
              const isHoje = dia === hoje.getDate() && mes === hoje.getMonth() && ano === hoje.getFullYear()
              const isSel = dia === diaSel
              const dataStr = `${ano}-${String(mes+1).padStart(2,'0')}-${String(dia).padStart(2,'0')}`
              const evento = eventos[dataStr]
              return (
                <button key={dia} onClick={() => setDiaSel(dia)}
                  className={`aspect-square flex flex-col items-center justify-center rounded-full text-sm transition ${
                    isSel ? 'bg-green-800 text-white' :
                    isHoje ? 'border-2 border-green-700 text-green-800 font-bold' :
                    'text-gray-800 hover:bg-gray-100'
                  }`}>
                  <span className={`text-xs ${isSel ? 'font-bold' : ''}`}>{dia}</span>
                  {evento && (
                    <div className={`w-1.5 h-1.5 rounded-full mt-0.5 ${isSel ? 'bg-white' : eventoCor(evento)}`} />
                  )}
                </button>
              )
            })}
          </div>

          {/* Legenda */}
          <div className="flex gap-3 flex-wrap mt-3 pt-3 border-t border-gray-50">
            {[['cio','Cio'],['inseminacao','Inseminação'],['parto','Parto'],['diagnostico','Diagnóstico']].map(([tipo, label]) => (
              <div key={tipo} className="flex items-center gap-1">
                <div className={`w-2.5 h-2.5 rounded-full ${eventoCor(tipo as TipoEvento)}`} />
                <span className="text-[10px] text-gray-500">{label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Próximos eventos */}
        <div className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm">
          <h3 className="font-bold text-sm text-gray-900 mb-3">Próximos eventos</h3>
          {alertas.length === 0 ? (
            <p className="text-sm text-gray-400 text-center py-4">Nenhum animal cadastrado ainda.</p>
          ) : alertas.map((a, i) => (
            <div key={i} className="flex items-center gap-3 py-2.5 border-b border-gray-50 last:border-0">
              <span className="text-xl">{a.emoji}</span>
              <div className="flex-1">
                <p className="text-sm font-medium text-gray-900">{a.nome}</p>
                <p className="text-xs text-gray-500">{a.desc}</p>
              </div>
              {eventoIcon(a.tipo)}
            </div>
          ))}
        </div>
      </div>

      {/* Dialog */}
      {showDialog && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-end justify-center p-4">
          <div className="bg-white rounded-3xl p-6 w-full max-w-sm">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-bold text-gray-900">Registrar Evento</h3>
              <button onClick={() => setShowDialog(false)}>
                <X className="w-5 h-5 text-gray-400" />
              </button>
            </div>

            {animais.length === 0 ? (
              <p className="text-gray-500 text-sm text-center py-4">Cadastre animais antes de registrar eventos.</p>
            ) : (
              <div className="space-y-3">
                <div>
                  <label className="text-xs font-semibold text-gray-600 mb-1 block">Animal</label>
                  <select value={formAnimal} onChange={e => setFormAnimal(e.target.value)}
                    className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm bg-gray-50 focus:outline-none focus:border-green-600">
                    <option value="">Selecione...</option>
                    {animais.map(a => <option key={a.id} value={a.id}>{a.nome} ({ESPECIE_EMOJI[a.especie]})</option>)}
                  </select>
                </div>

                <div>
                  <label className="text-xs font-semibold text-gray-600 mb-1 block">Tipo de evento</label>
                  <div className="flex flex-wrap gap-2">
                    {(['cio','inseminacao','diagnostico','parto','desmame'] as TipoEvento[]).map(t => (
                      <button key={t} onClick={() => setFormTipo(t)}
                        className={`px-3 py-1.5 rounded-xl text-xs font-bold transition ${
                          formTipo === t ? 'bg-green-800 text-white' : 'bg-gray-100 text-gray-600'
                        }`}>
                        {t.charAt(0).toUpperCase() + t.slice(1)}
                      </button>
                    ))}
                  </div>
                </div>

                <div>
                  <label className="text-xs font-semibold text-gray-600 mb-1 block">Observações</label>
                  <input type="text" value={formObs} onChange={e => setFormObs(e.target.value)}
                    placeholder="Opcional..."
                    className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm bg-gray-50 focus:outline-none focus:border-green-600" />
                </div>

                <div className="flex gap-3 mt-2">
                  <button onClick={() => setShowDialog(false)}
                    className="flex-1 py-3 border border-gray-200 rounded-xl text-sm font-medium text-gray-600">
                    Cancelar
                  </button>
                  <button onClick={salvarEvento} disabled={!formAnimal || saving}
                    className="flex-1 py-3 bg-green-800 text-white rounded-xl text-sm font-bold disabled:opacity-50">
                    {saving ? 'Salvando...' : 'Salvar'}
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
