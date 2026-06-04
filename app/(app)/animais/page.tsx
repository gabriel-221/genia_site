'use client'
import { useEffect, useState } from 'react'
import Link from 'next/link'
import { createClient } from '@/lib/supabase/client'
import type { Animal, Especie } from '@/types'
import { ESPECIE_EMOJI, ESPECIE_LABEL } from '@/types'
import { Plus, Search, ChevronRight, Dna } from '@/components/Icons'

export default function AnimaisPage() {
  const supabase = createClient()
  const [animais, setAnimais] = useState<Animal[]>([])
  const [filtroEspecie, setFiltroEspecie] = useState<Especie | null>(null)
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) return
      const { data: prod } = await supabase.from('genia_produtor').select('id').eq('user_id', user.id).single()
      if (!prod) { setLoading(false); return }
      const { data } = await supabase.from('genia_animal').select('*').eq('produtor_id', prod.id).order('nome')
      setAnimais(data ?? [])
      setLoading(false)
    }
    load()
  }, [])

  const filtered = animais.filter(a =>
    (!filtroEspecie || a.especie === filtroEspecie) &&
    (!query || a.nome.toLowerCase().includes(query.toLowerCase()) || a.raca.toLowerCase().includes(query.toLowerCase()))
  )

  function idadeAnimal(dataNasc?: string) {
    if (!dataNasc) return ''
    const anos = (Date.now() - new Date(dataNasc).getTime()) / (365.25 * 24 * 3600 * 1000)
    if (anos < 1) return `${Math.round(anos * 12)}m`
    return `${Math.floor(anos)}a`
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white px-5 pt-5 pb-3 border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-lg mx-auto">
          <div className="flex items-center justify-between mb-3">
            <h1 className="text-xl font-bold text-gray-900">Animais</h1>
            <Link href="/animais/novo"
              className="bg-green-800 text-white text-sm font-bold px-4 py-2 rounded-xl flex items-center gap-1">
              <Plus className="w-4 h-4" /> Novo
            </Link>
          </div>

          {/* Search */}
          <div className="relative mb-3">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" />
            <input
              type="text"
              placeholder="Buscar por nome ou raça..."
              value={query}
              onChange={e => setQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2.5 bg-gray-100 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-green-600"
            />
          </div>

          {/* Filtro espécie */}
          <div className="flex gap-2 overflow-x-auto pb-1">
            {[null, 'bovino', 'ovino', 'caprino'].map(esp => (
              <button
                key={esp ?? 'all'}
                onClick={() => setFiltroEspecie(esp as Especie | null)}
                className={`shrink-0 px-3 py-1.5 rounded-xl text-xs font-bold transition ${
                  filtroEspecie === esp
                    ? 'bg-green-800 text-white'
                    : 'bg-gray-100 text-gray-600'
                }`}
              >
                {esp ? `${ESPECIE_EMOJI[esp as Especie]} ${ESPECIE_LABEL[esp as Especie]}` : 'Todos'}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="max-w-lg mx-auto px-4 py-4">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="w-8 h-8 border-2 border-green-800 border-t-transparent rounded-full animate-spin" />
          </div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-16">
            <span className="text-5xl">🐄</span>
            <p className="text-gray-500 mt-3">Nenhum animal encontrado</p>
            {animais.length === 0 && (
              <Link href="/animais/novo"
                className="inline-block mt-4 bg-green-800 text-white text-sm font-bold px-6 py-2.5 rounded-xl">
                + Cadastrar primeiro animal
              </Link>
            )}
          </div>
        ) : (
          <div className="space-y-2">
            <p className="text-xs text-gray-400 mb-2">{filtered.length} animal{filtered.length !== 1 ? 'is' : ''}</p>
            {filtered.map(animal => (
              <Link key={animal.id} href={`/animais/${animal.id}`}
                className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm flex items-center gap-3 active:bg-gray-50">
                {/* Emoji */}
                <div className="w-12 h-12 bg-green-100 rounded-2xl flex items-center justify-center text-2xl shrink-0">
                  {ESPECIE_EMOJI[animal.especie]}
                </div>
                {/* Info */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <p className="font-bold text-gray-900 truncate">{animal.nome}</p>
                    {animal.prenhou && (
                      <span className="bg-pink-100 text-pink-700 text-[10px] font-bold px-1.5 py-0.5 rounded-full shrink-0">
                        Gestante
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-gray-500 truncate">{animal.raca} · {ESPECIE_LABEL[animal.especie]}</p>
                  <div className="flex items-center gap-3 mt-1">
                    <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded-full ${
                      animal.sexo === 'femea' ? 'bg-pink-50 text-pink-600' : 'bg-blue-50 text-blue-600'
                    }`}>
                      {animal.sexo === 'femea' ? '♀ Fêmea' : '♂ Macho'}
                    </span>
                    {animal.peso_kg && <span className="text-[10px] text-gray-400">{animal.peso_kg}kg</span>}
                    {animal.data_nascimento && <span className="text-[10px] text-gray-400">{idadeAnimal(animal.data_nascimento)}</span>}
                    {animal.disponivel_match && <Dna className="w-3 h-3 text-green-600" />}
                  </div>
                </div>
                <ChevronRight className="w-4 h-4 text-gray-300 shrink-0" />
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
