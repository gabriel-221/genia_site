'use client'
import { useEffect, useState, useCallback } from 'react'
import { createClient } from '@/lib/supabase/client'
import type { Animal } from '@/types'
import { ESPECIE_EMOJI } from '@/types'
import { ChevronLeft, ChevronRight, Plus, Heart, Syringe, Baby, Microscope, X } from '@/components/Icons'

const MESES_PT = ['Janeiro','Fevereiro','Março','Abril','Maio','Junho',
                  'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro']
const DIAS_PT  = ['Dom','Seg','Ter','Qua','Qui','Sex','Sáb']

type TipoEvento = 'cio' | 'inseminacao' | 'diagnostico' | 'parto' | 'desmame'

const TIPO_LABEL: Record<TipoEvento, string> = {
  cio: 'Cio', inseminacao: 'Inseminação', diagnostico: 'Diagnóstico', parto: 'Parto', desmame: 'Desmame',
}

function toDateStr(d: Date) {
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

function eventoIcon(tipo: TipoEvento) {
  const cls = 'w-3.5 h-3.5'
  if (tipo === 'cio')         return <div className={`${cls} rounded-full bg-green-500`} />
  if (tipo === 'inseminacao') return <Syringe   className={`${cls} text-blue-600`} />
  if (tipo === 'diagnostico') return <Microscope className={`${cls} text-purple-600`} />
  if (tipo === 'parto')       return <Baby      className={`${cls} text-orange-500`} />
  return <Heart className={`${cls} text-pink-500`} />
}

function eventoCor(tipo: TipoEvento) {
  if (tipo === 'cio')         return 'bg-green-500'
  if (tipo === 'inseminacao') return 'bg-blue-500'
  if (tipo === 'diagnostico') return 'bg-purple-500'
  if (tipo === 'parto')       return 'bg-orange-500'
  return 'bg-pink-500'
}

interface EventoDB {
  id: string
  animal_id: string
  tipo: TipoEvento
  data_evento: string
  observacoes?: string
}

export default function CalendarioPage() {
  const supabase    = createClient()
  const hoje        = new Date()
  const [mes, setMes]     = useState(hoje.getMonth())
  const [ano, setAno]     = useState(hoje.getFullYear())
  const [diaSel, setDiaSel] = useState<number | null>(null)
  const [animais, setAnimais]   = useState<Animal[]>([])
  const [produtorId, setProdutorId] = useState<string | null>(null)

  // Eventos reais do banco: mapa de data -> array de tipos
  const [eventosDB, setEventosDB] = useState<Record<string, TipoEvento[]>>({})

  // Dialog
  const [showDialog, setShowDialog]   = useState(false)
  const [saving, setSaving]           = useState(false)
  const [erro, setErro]               = useState('')
  const [sucesso, setSucesso]         = useState(false)
  const [formAnimal, setFormAnimal]   = useState('')
  const [formTipo, setFormTipo]       = useState<TipoEvento>('cio')
  const [formData, setFormData]       = useState(toDateStr(hoje))
  const [formObs, setFormObs]         = useState('')

  // Carrega animais e produtor
  useEffect(() => {
    async function loadAnimais() {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) return
      const { data: prod } = await supabase
        .from('genia_produtor').select('id').eq('user_id', user.id).single()
      if (!prod) return
      setProdutorId(prod.id)
      const { data: anim } = await supabase
        .from('genia_animal').select('*').eq('produtor_id', prod.id).order('nome')
      setAnimais(anim ?? [])
    }
    loadAnimais()
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  // Carrega eventos reais do mês atual
  const loadEventos = useCallback(async () => {
    if (!produtorId) return
    const inicio = `${ano}-${String(mes+1).padStart(2,'0')}-01`
    const fim    = `${ano}-${String(mes+1).padStart(2,'0')}-${new Date(ano, mes+1, 0).getDate()}`

    const { data, error } = await supabase
      .from('genia_evento_reprodutivo')
      .select('id, animal_id, tipo, data_evento, observacoes')
      .gte('data_evento', inicio)
      .lte('data_evento', fim)
      .order('data_evento')

    if (error) { console.error('Erro ao carregar eventos:', error); return }

    const map: Record<string, TipoEvento[]> = {}
    ;(data as EventoDB[] ?? []).forEach(ev => {
      if (!map[ev.data_evento]) map[ev.data_evento] = []
      map[ev.data_evento].push(ev.tipo)
    })
    setEventosDB(map)
  }, [produtorId, mes, ano]) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => { loadEventos() }, [loadEventos])

  // Abre dialog ao clicar num dia — preenche a data automaticamente
  function handleDiaClick(dia: number) {
    setDiaSel(dia)
    const ds = `${ano}-${String(mes+1).padStart(2,'0')}-${String(dia).padStart(2,'0')}`
    setFormData(ds)
    setFormAnimal('')
    setFormTipo('cio')
    setFormObs('')
    setErro('')
    setSucesso(false)
    setShowDialog(true)
  }

  // Abre dialog via botão "Registrar" (sem dia pré-selecionado)
  function abrirDialog() {
    setFormData(toDateStr(hoje))
    setFormAnimal('')
    setFormTipo('cio')
    setFormObs('')
    setErro('')
    setSucesso(false)
    setShowDialog(true)
  }

  function fecharDialog() {
    setShowDialog(false)
    setErro('')
    setSucesso(false)
  }

  async function salvarEvento() {
    if (!formAnimal) { setErro('Selecione um animal.'); return }
    if (!formData)   { setErro('Informe a data do evento.'); return }
    if (!produtorId) { setErro('Produtor não identificado.'); return }

    setSaving(true); setErro('')

    // Verifica sessão antes do insert
    const { data: { session } } = await supabase.auth.getSession()
    if (!session) {
      setSaving(false)
      setErro('Sessão expirada. Faça logout e login novamente.')
      return
    }

    const { error } = await supabase.from('genia_evento_reprodutivo').insert({
      animal_id:        formAnimal,
      tipo:             formTipo,
      data_evento:      formData,
      semen_reprodutor: '',
      observacoes:      formObs.trim() || null,
    })

    setSaving(false)

    if (error) {
      setErro('Erro ao salvar: ' + error.message)
      return
    }

    setSucesso(true)
    // Atualiza o mapa local imediatamente
    setEventosDB(prev => {
      const arr = [...(prev[formData] ?? []), formTipo]
      return { ...prev, [formData]: arr }
    })
    // Recarrega do banco para garantir consistência
    await loadEventos()

    setTimeout(() => {
      fecharDialog()
    }, 800)
  }

  function prevMes() {
    if (mes === 0) { setMes(11); setAno(a => a - 1) } else setMes(m => m - 1)
  }
  function nextMes() {
    if (mes === 11) { setMes(0); setAno(a => a + 1) } else setMes(m => m + 1)
  }

  const primeiroDia = new Date(ano, mes, 1).getDay()
  const diasNoMes   = new Date(ano, mes + 1, 0).getDate()

  // "Próximos eventos" — baseado nos eventos reais do banco no mês
  const proximosEventos: { data: string; tipo: TipoEvento; animal: Animal | undefined }[] = []
  Object.entries(eventosDB)
    .sort(([a], [b]) => a.localeCompare(b))
    .forEach(([data, tipos]) => {
      tipos.forEach(tipo => proximosEventos.push({ data, tipo, animal: undefined }))
    })

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white px-5 py-4 border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-lg mx-auto flex items-center justify-between">
          <h1 className="text-xl font-bold text-gray-900">Calendário</h1>
          <button onClick={abrirDialog}
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
            <button onClick={prevMes} className="p-2 text-gray-500 hover:bg-gray-100 rounded-xl">
              <ChevronLeft className="w-5 h-5" />
            </button>
            <p className="font-bold text-gray-900">{MESES_PT[mes]} {ano}</p>
            <button onClick={nextMes} className="p-2 text-gray-500 hover:bg-gray-100 rounded-xl">
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>

          {/* Dias da semana */}
          <div className="grid grid-cols-7 mb-2">
            {DIAS_PT.map(d => (
              <div key={d} className="text-center text-[10px] text-gray-400 font-medium">{d}</div>
            ))}
          </div>

          {/* Grid de dias */}
          <div className="grid grid-cols-7 gap-y-1">
            {Array(primeiroDia).fill(null).map((_, i) => <div key={`e${i}`} />)}
            {Array(diasNoMes).fill(null).map((_, i) => {
              const dia    = i + 1
              const isHoje = dia === hoje.getDate() && mes === hoje.getMonth() && ano === hoje.getFullYear()
              const isSel  = dia === diaSel
              const dataStr = `${ano}-${String(mes+1).padStart(2,'0')}-${String(dia).padStart(2,'0')}`
              const tipos   = eventosDB[dataStr] ?? []
              // Mostra apenas o primeiro tipo como marcador (com ponto)
              const tipoMarcador = tipos[0]
              return (
                <button
                  key={dia}
                  onClick={() => handleDiaClick(dia)}
                  className={`aspect-square flex flex-col items-center justify-center rounded-full text-sm transition active:scale-95 ${
                    isSel  ? 'bg-green-800 text-white' :
                    isHoje ? 'border-2 border-green-700 text-green-800 font-bold' :
                    'text-gray-800 hover:bg-gray-100'
                  }`}
                >
                  <span className={`text-xs ${isSel ? 'font-bold' : ''}`}>{dia}</span>
                  {tipoMarcador && (
                    <div className={`w-1.5 h-1.5 rounded-full mt-0.5 ${
                      isSel ? 'bg-white' : eventoCor(tipoMarcador)
                    }`} />
                  )}
                  {/* Badge se houver mais de 1 evento no dia */}
                  {tipos.length > 1 && !isSel && (
                    <span className="text-[8px] text-gray-400 leading-none">{tipos.length}</span>
                  )}
                </button>
              )
            })}
          </div>

          {/* Legenda */}
          <div className="flex gap-3 flex-wrap mt-3 pt-3 border-t border-gray-50">
            {(['cio','inseminacao','parto','diagnostico','desmame'] as TipoEvento[]).map(tipo => (
              <div key={tipo} className="flex items-center gap-1">
                <div className={`w-2 h-2 rounded-full ${eventoCor(tipo)}`} />
                <span className="text-[10px] text-gray-500">{TIPO_LABEL[tipo]}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Eventos do mês */}
        <div className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm">
          <h3 className="font-bold text-sm text-gray-900 mb-3">
            Eventos de {MESES_PT[mes]}
            {proximosEventos.length > 0 && (
              <span className="ml-2 text-xs font-normal text-gray-400">({proximosEventos.length})</span>
            )}
          </h3>
          {proximosEventos.length === 0 ? (
            <div className="text-center py-6">
              <p className="text-sm text-gray-400">Nenhum evento registrado neste mês.</p>
              <button onClick={abrirDialog}
                className="mt-3 text-xs text-green-700 font-semibold underline underline-offset-2">
                + Registrar primeiro evento
              </button>
            </div>
          ) : (
            <div className="space-y-0">
              {proximosEventos.slice(0, 10).map((ev, i) => {
                const [, , d] = ev.data.split('-')
                return (
                  <div key={i} className="flex items-center gap-3 py-2.5 border-b border-gray-50 last:border-0">
                    <div className={`w-8 h-8 rounded-xl flex items-center justify-center text-white text-xs font-bold shrink-0 ${eventoCor(ev.tipo)}`}>
                      {d}
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-medium text-gray-900">{TIPO_LABEL[ev.tipo]}</p>
                      <p className="text-xs text-gray-400">{ev.data}</p>
                    </div>
                    {eventoIcon(ev.tipo)}
                  </div>
                )
              })}
            </div>
          )}
        </div>

        {/* Animais sem eventos (alertas) */}
        {animais.filter(a => a.prenhou || a.sexo === 'femea').length > 0 && (
          <div className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm">
            <h3 className="font-bold text-sm text-gray-900 mb-3">Atenção no rebanho</h3>
            <div className="space-y-0">
              {animais.filter(a => a.prenhou || a.sexo === 'femea').slice(0, 5).map(a => (
                <div key={a.id} className="flex items-center gap-3 py-2.5 border-b border-gray-50 last:border-0">
                  <span className="text-xl">{ESPECIE_EMOJI[a.especie]}</span>
                  <div className="flex-1">
                    <p className="text-sm font-medium text-gray-900">{a.nome}</p>
                    <p className="text-xs text-gray-500">
                      {a.prenhou ? 'Gestação confirmada — acompanhar parto' : 'Fêmea — monitorar cio'}
                    </p>
                  </div>
                  {eventoIcon(a.prenhou ? 'parto' : 'cio')}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Dialog de registro */}
      {showDialog && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-end justify-center p-4"
          onClick={e => { if (e.target === e.currentTarget) fecharDialog() }}>
          <div className="bg-white rounded-3xl p-6 w-full max-w-sm shadow-2xl">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-bold text-gray-900">Registrar Evento</h3>
              <button onClick={fecharDialog} className="p-1">
                <X className="w-5 h-5 text-gray-400" />
              </button>
            </div>

            {animais.length === 0 ? (
              <p className="text-gray-500 text-sm text-center py-4">
                Cadastre animais antes de registrar eventos.
              </p>
            ) : sucesso ? (
              <div className="text-center py-6">
                <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-3">
                  <span className="text-2xl">✅</span>
                </div>
                <p className="text-green-800 font-bold">Evento registrado!</p>
                <p className="text-sm text-gray-500 mt-1">{TIPO_LABEL[formTipo]} em {formData}</p>
              </div>
            ) : (
              <div className="space-y-4">
                {/* Data */}
                <div>
                  <label className="text-xs font-semibold text-gray-600 mb-1 block">Data do evento *</label>
                  <input
                    type="date"
                    value={formData}
                    onChange={e => setFormData(e.target.value)}
                    className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm bg-gray-50 focus:outline-none focus:border-green-600 focus:ring-1 focus:ring-green-600"
                  />
                </div>

                {/* Animal */}
                <div>
                  <label className="text-xs font-semibold text-gray-600 mb-1 block">Animal *</label>
                  <select
                    value={formAnimal}
                    onChange={e => setFormAnimal(e.target.value)}
                    className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm bg-gray-50 focus:outline-none focus:border-green-600 focus:ring-1 focus:ring-green-600"
                  >
                    <option value="">Selecione o animal...</option>
                    {animais.map(a => (
                      <option key={a.id} value={a.id}>
                        {ESPECIE_EMOJI[a.especie]} {a.nome} — {a.raca}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Tipo de evento */}
                <div>
                  <label className="text-xs font-semibold text-gray-600 mb-2 block">Tipo de evento *</label>
                  <div className="grid grid-cols-3 gap-2">
                    {(['cio','inseminacao','diagnostico','parto','desmame'] as TipoEvento[]).map(t => (
                      <button
                        key={t}
                        onClick={() => setFormTipo(t)}
                        className={`py-2 px-2 rounded-xl text-xs font-bold transition border ${
                          formTipo === t
                            ? 'bg-green-800 border-green-800 text-white'
                            : 'bg-white border-gray-200 text-gray-600 hover:border-green-400'
                        }`}
                      >
                        {TIPO_LABEL[t]}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Observações */}
                <div>
                  <label className="text-xs font-semibold text-gray-600 mb-1 block">Observações</label>
                  <input
                    type="text"
                    value={formObs}
                    onChange={e => setFormObs(e.target.value)}
                    placeholder="Opcional..."
                    className="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm bg-gray-50 focus:outline-none focus:border-green-600 focus:ring-1 focus:ring-green-600"
                  />
                </div>

                {erro && (
                  <p className="text-red-600 text-xs bg-red-50 border border-red-100 rounded-xl px-3 py-2">
                    {erro}
                  </p>
                )}

                <div className="flex gap-3 pt-1">
                  <button onClick={fecharDialog}
                    className="flex-1 py-3 border border-gray-200 rounded-xl text-sm font-medium text-gray-600">
                    Cancelar
                  </button>
                  <button
                    onClick={salvarEvento}
                    disabled={!formAnimal || !formData || saving}
                    className="flex-1 py-3 bg-green-800 text-white rounded-xl text-sm font-bold disabled:opacity-50 flex items-center justify-center gap-2"
                  >
                    {saving
                      ? <><span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> Salvando...</>
                      : 'Salvar'}
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
