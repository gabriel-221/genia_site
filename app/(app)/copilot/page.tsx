'use client'
import { useEffect, useRef, useState } from 'react'
import { createClient } from '@/lib/supabase/client'
import type { Animal } from '@/types'
import { ESPECIE_LABEL } from '@/types'
import { Sparkles, Send, ArrowLeft } from '@/components/Icons'

interface Msg { role: 'user' | 'assistant'; content: string }

const SUGESTOES = [
  'Qual a situação do meu rebanho?',
  'Quais animais têm mais potencial para leite?',
  'Como melhorar minha taxa de prenhez?',
  'Dicas para manejo de caprinos no semiárido',
]

export default function CopilotPage() {
  const supabase = createClient()
  const bottomRef = useRef<HTMLDivElement>(null)
  const [animais, setAnimais] = useState<Animal[]>([])
  const [msgs, setMsgs] = useState<Msg[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [iniciado, setIniciado] = useState(false)

  useEffect(() => {
    async function load() {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) return
      const { data: prod } = await supabase.from('genia_produtor').select('id,nome').eq('user_id', user.id).single()
      if (!prod) return
      const { data } = await supabase.from('genia_animal').select('*').eq('produtor_id', prod.id)
      const anim = data ?? []
      setAnimais(anim)

      const total = anim.length
      const femeas = anim.filter((a: Animal) => a.sexo === 'femea').length
      const prenhas = anim.filter((a: Animal) => a.prenhou).length
      const msg = total === 0
        ? 'Olá! Sou o GENIA Copilot. Cadastre seus animais para eu poder dar recomendações personalizadas. Como posso ajudar?'
        : `Olá! Sou o GENIA Copilot. Identifiquei ${total} animais no seu rebanho (${femeas} fêmeas, ${prenhas} gestantes). Como posso ajudar você hoje?`

      setMsgs([{ role: 'assistant', content: msg }])
    }
    load()
  }, [])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [msgs, loading])

  function buildResumo() {
    const total = animais.length
    if (total === 0) return 'Nenhum animal cadastrado ainda.'
    const femeas = animais.filter(a => a.sexo === 'femea').length
    const machos = animais.filter(a => a.sexo === 'macho').length
    const prenhas = animais.filter(a => a.prenhou).length
    const dispMatch = animais.filter(a => a.disponivel_match).length
    const esp = animais.reduce((acc, a) => {
      acc[a.especie] = (acc[a.especie] || 0) + 1; return acc
    }, {} as Record<string, number>)
    const espStr = Object.entries(esp).map(([e, n]) => `${n} ${ESPECIE_LABEL[e as keyof typeof ESPECIE_LABEL]}`).join(', ')

    const topAnim = animais.slice(0, 8).map(a =>
      `- ${a.nome} (${ESPECIE_LABEL[a.especie]}, ${a.raca}, ${a.sexo === 'femea' ? '♀' : '♂'}` +
      (a.peso_kg ? `, ${a.peso_kg}kg` : '') +
      ((a.producao_leite_diaria ?? 0) > 0 ? `, ${a.producao_leite_diaria}L/dia` : '') +
      (a.prenhou ? ', gestante' : '') + ')'
    ).join('\n')

    return `Total: ${total} (${femeas} fêmeas, ${machos} machos)\nEspécies: ${espStr}\nGestantes: ${prenhas}\nGeneMatch: ${dispMatch}\n\nAnimais:\n${topAnim}`
  }

  async function enviar(texto: string) {
    if (!texto.trim() || loading) return
    setIniciado(true)
    const novaMsg: Msg = { role: 'user', content: texto }
    const historicoAtual = [...msgs.filter(m => m.content.trim()), novaMsg]
    setMsgs(prev => [...prev, novaMsg])
    setInput('')
    setLoading(true)

    try {
      const res = await fetch('/api/copilot', {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({
          mensagem: texto,
          historico: historicoAtual.slice(-10).map(m => ({ role: m.role, content: m.content })),
          resumoRebanho: buildResumo(),
        }),
      })
      const data = await res.json()
      setMsgs(prev => [...prev, { role: 'assistant', content: data.resposta ?? 'Erro ao processar.' }])
    } catch {
      setMsgs(prev => [...prev, { role: 'assistant', content: 'Erro de conexão. Verifique sua internet.' }])
    }
    setLoading(false)
  }

  return (
    <div className="flex flex-col h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-green-800 px-5 py-4 shrink-0">
        <div className="max-w-lg mx-auto flex items-center gap-3">
          <div className="w-10 h-10 bg-green-700 rounded-xl flex items-center justify-center">
            <Sparkles className="w-5 h-5 text-white" />
          </div>
          <div>
            <p className="text-white font-extrabold text-lg">GENIA Copilot</p>
            <p className="text-green-300 text-xs">IA com dados do seu rebanho</p>
          </div>
          <div className="ml-auto bg-green-700 text-white text-xs px-3 py-1 rounded-full">
            {animais.length} animais
          </div>
        </div>
      </div>

      {/* Mensagens */}
      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-3 max-w-lg mx-auto w-full">
        {msgs.map((m, i) => (
          <div key={i} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'} items-end gap-2`}>
            {m.role === 'assistant' && (
              <div className="w-8 h-8 bg-green-100 rounded-xl flex items-center justify-center shrink-0">
                <Sparkles className="w-4 h-4 text-green-800" />
              </div>
            )}
            <div className={`max-w-[82%] px-4 py-3 rounded-2xl text-sm leading-relaxed whitespace-pre-wrap ${
              m.role === 'user'
                ? 'bg-green-800 text-white rounded-br-md'
                : 'bg-white text-gray-900 shadow-sm rounded-bl-md border border-gray-100'
            }`}>
              {m.content}
            </div>
          </div>
        ))}

        {loading && (
          <div className="flex items-end gap-2">
            <div className="w-8 h-8 bg-green-100 rounded-xl flex items-center justify-center">
              <Sparkles className="w-4 h-4 text-green-800" />
            </div>
            <div className="bg-white shadow-sm border border-gray-100 px-4 py-3 rounded-2xl rounded-bl-md">
              <div className="flex gap-1 items-center h-4">
                {[0,1,2].map(i => (
                  <div key={i} className="w-2 h-2 bg-green-600 rounded-full animate-bounce" style={{ animationDelay: `${i*0.15}s` }} />
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Sugestões (só no início) */}
        {!iniciado && msgs.length > 0 && (
          <div className="space-y-2 pt-2">
            <p className="text-xs text-gray-400 text-center">Sugestões para começar</p>
            {SUGESTOES.map(s => (
              <button key={s} onClick={() => enviar(s)}
                className="w-full text-left bg-white border border-gray-100 rounded-2xl px-4 py-3 text-sm text-gray-700 shadow-sm flex items-center gap-2 hover:bg-gray-50 active:bg-gray-100">
                <Sparkles className="w-4 h-4 text-green-600 shrink-0" />
                {s}
              </button>
            ))}
          </div>
        )}

        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <div className="bg-white border-t border-gray-100 px-4 py-3 pb-safe shrink-0">
        <div className="max-w-lg mx-auto flex gap-2 items-end">
          <textarea
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={e => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); enviar(input) } }}
            placeholder="Pergunte sobre seu rebanho..."
            rows={1}
            disabled={loading}
            className="flex-1 resize-none border border-gray-200 rounded-2xl px-4 py-3 text-sm bg-gray-50 focus:outline-none focus:border-green-600 focus:ring-1 focus:ring-green-600 max-h-24 disabled:opacity-50"
            style={{ fieldSizing: 'content' } as React.CSSProperties}
          />
          <button
            onClick={() => enviar(input)}
            disabled={!input.trim() || loading}
            className="w-11 h-11 bg-green-800 text-white rounded-xl flex items-center justify-center disabled:opacity-40 shrink-0 transition hover:bg-green-700"
          >
            <Send className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  )
}
