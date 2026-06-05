'use client'
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { createClient } from '@/lib/supabase/client'
import type { Animal, Produtor } from '@/types'
import { ESPECIE_EMOJI, ESPECIE_LABEL } from '@/types'
import { PawPrint, Heart, Sparkles, Users, ChevronRight, LogOut } from '@/components/Icons'

export default function DashboardPage() {
  const router = useRouter()
  const supabase = createClient()
  const [produtor, setProdutor] = useState<Produtor | null>(null)
  const [animais, setAnimais] = useState<Animal[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) { router.push('/login'); return }

      const { data: prod } = await supabase
        .from('genia_produtor').select('*').eq('user_id', user.id).single()

      setProdutor(prod)

      if (prod?.id) {
        const { data: animals } = await supabase
          .from('genia_animal').select('*').eq('produtor_id', prod.id)
        setAnimais(animals ?? [])
      }
      setLoading(false)
    }
    load()
  }, [])

  async function handleLogout() {
    await supabase.auth.signOut()
    document.cookie = 'genia-session=; path=/; max-age=0; SameSite=Lax'
    router.push('/login')
    router.refresh()
  }

  const femeas    = animais.filter(a => a.sexo === 'femea')
  const prenhas   = animais.filter(a => a.prenhou)
  const taxaPrenhez = femeas.length > 0 ? Math.round((prenhas.length / femeas.length) * 100) : 0
  const dispMatch = animais.filter(a => a.disponivel_match).length

  const especieMap = animais.reduce((acc, a) => {
    acc[a.especie] = (acc[a.especie] || 0) + 1; return acc
  }, {} as Record<string, number>)

  const primeiroNome = produtor?.nome?.split(' ')[0] ?? 'Produtor'
  const fazenda = produtor?.nome_fazenda || produtor?.municipio || 'GENIA'

  if (loading) return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="w-8 h-8 border-2 border-green-800 border-t-transparent rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white px-5 py-4 border-b border-gray-100">
        <div className="flex items-center justify-between max-w-lg mx-auto">
          <div>
            <p className="text-xl font-bold text-gray-900">Olá, {primeiroNome}!</p>
            <p className="text-sm text-gray-500">{fazenda}</p>
          </div>
          <div className="flex items-center gap-2">
            <Link href="/relatorios" className="bg-green-800 text-white text-xs font-bold px-4 py-2 rounded-xl flex items-center gap-1">
              <span>Relatórios</span>
            </Link>
            <button onClick={handleLogout} className="p-2 text-gray-400 hover:text-red-500">
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-lg mx-auto px-4 py-4 space-y-4">
        {/* KPIs */}
        <div className="grid grid-cols-2 gap-3">
          <div className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100">
            <PawPrint className="w-5 h-5 text-green-700 mb-2" />
            <p className="text-2xl font-extrabold text-gray-900">{animais.length || '—'}</p>
            <p className="text-xs text-gray-500">Total de animais</p>
          </div>
          <div className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100">
            <Heart className="w-5 h-5 text-red-500 mb-2" />
            <p className="text-2xl font-extrabold text-gray-900">
              {femeas.length > 0 ? `${taxaPrenhez}%` : '—'}
            </p>
            <p className="text-xs text-gray-500">Taxa de prenhez</p>
          </div>
          <div className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100">
            <Users className="w-5 h-5 text-green-600 mb-2" />
            <p className="text-2xl font-extrabold text-gray-900">{dispMatch || '—'}</p>
            <p className="text-xs text-gray-500">No GeneMatch</p>
          </div>
          <div className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100">
            <span className="text-xl mb-2 block">🐄</span>
            <p className="text-2xl font-extrabold text-gray-900">
              {Object.keys(especieMap).length || '—'}
            </p>
            <p className="text-xs text-gray-500">Espécies</p>
          </div>
        </div>

        {/* Espécies */}
        {animais.length > 0 && (
          <div className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100">
            <div className="flex items-center justify-between mb-3">
              <h3 className="font-semibold text-gray-900 text-sm">Animais por espécie</h3>
              <Link href="/animais" className="text-green-700 text-xs flex items-center gap-0.5">
                Ver todos <ChevronRight className="w-3 h-3" />
              </Link>
            </div>
            <div className="space-y-2">
              {(['bovino', 'ovino', 'caprino'] as const).map(esp => {
                const qtd = especieMap[esp] || 0
                if (qtd === 0) return null
                const pct = Math.round((qtd / animais.length) * 100)
                return (
                  <div key={esp}>
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-sm flex items-center gap-2">
                        {ESPECIE_EMOJI[esp]} {ESPECIE_LABEL[esp]}
                      </span>
                      <span className="text-sm font-semibold">{qtd} ({pct}%)</span>
                    </div>
                    <div className="h-1.5 bg-green-100 rounded-full">
                      <div className="h-1.5 bg-green-700 rounded-full" style={{ width: `${pct}%` }} />
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        )}

        {/* Copilot card */}
        <Link href="/copilot" className="block bg-green-800 rounded-2xl p-4 shadow-sm">
          <div className="flex items-start gap-3">
            <Sparkles className="w-7 h-7 text-white shrink-0 mt-0.5" />
            <div>
              <p className="text-white font-bold text-sm">GENIA Copilot</p>
              <p className="text-green-200 text-xs mt-1 leading-relaxed">
                {animais.length === 0
                  ? 'Cadastre seus animais e peça recomendações genéticas personalizadas.'
                  : taxaPrenhez < 50
                  ? `Taxa de prenhez em ${taxaPrenhez}%. Converse com o Copilot sobre melhorias.`
                  : `Seu rebanho tem ${animais.length} animais. Pergunte sobre o potencial genético!`}
              </p>
              <span className="inline-block mt-2 text-green-300 text-xs border border-green-600 rounded-lg px-3 py-1">
                Abrir chat →
              </span>
            </div>
          </div>
        </Link>

        {/* Empty state */}
        {animais.length === 0 && (
          <div className="bg-white rounded-2xl p-8 shadow-sm border border-gray-100 text-center">
            <span className="text-5xl">🐄</span>
            <p className="text-gray-600 font-medium mt-3">Nenhum animal cadastrado</p>
            <p className="text-gray-400 text-sm mt-1">Comece cadastrando seu primeiro animal</p>
            <Link href="/animais/novo"
              className="inline-block mt-4 bg-green-800 text-white text-sm font-bold px-6 py-2.5 rounded-xl">
              + Cadastrar animal
            </Link>
          </div>
        )}
      </div>
    </div>
  )
}
