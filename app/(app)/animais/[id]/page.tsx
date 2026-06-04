'use client'
import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import Link from 'next/link'
import { createClient } from '@/lib/supabase/client'
import type { Animal } from '@/types'
import { ESPECIE_EMOJI, ESPECIE_LABEL } from '@/types'
import { ArrowLeft, Pencil, Trash2, Heart, Dna } from '@/components/Icons'

function InfoRow({ label, value }: { label: string; value?: string | number | null }) {
  if (!value && value !== 0) return null
  return (
    <div className="flex justify-between py-2.5 border-b border-gray-50 last:border-0">
      <span className="text-sm text-gray-500">{label}</span>
      <span className="text-sm font-medium text-gray-900">{value}</span>
    </div>
  )
}

export default function AnimalDetalhePage() {
  const { id } = useParams<{ id: string }>()
  const router = useRouter()
  const supabase = createClient()
  const [animal, setAnimal] = useState<Animal | null>(null)
  const [loading, setLoading] = useState(true)
  const [confirmDelete, setConfirmDelete] = useState(false)

  useEffect(() => {
    supabase.from('genia_animal').select('*').eq('id', id).single()
      .then(({ data }) => { setAnimal(data); setLoading(false) })
  }, [id])

  async function handleDelete() {
    if (!animal) return
    await supabase.from('genia_animal').delete().eq('id', animal.id)
    router.push('/animais')
  }

  function formatDate(d?: string) {
    if (!d) return null
    return new Date(d).toLocaleDateString('pt-BR')
  }

  function idadeAnimal(dataNasc?: string) {
    if (!dataNasc) return null
    const anos = (Date.now() - new Date(dataNasc).getTime()) / (365.25 * 24 * 3600 * 1000)
    if (anos < 1) return `${Math.round(anos * 12)} meses`
    return `${Math.floor(anos)} ${Math.floor(anos) === 1 ? 'ano' : 'anos'}`
  }

  if (loading) return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="w-8 h-8 border-2 border-green-800 border-t-transparent rounded-full animate-spin" />
    </div>
  )

  if (!animal) return (
    <div className="flex flex-col items-center justify-center min-h-screen gap-4">
      <p className="text-gray-500">Animal não encontrado.</p>
      <Link href="/animais" className="text-green-700 font-medium">← Voltar</Link>
    </div>
  )

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header verde */}
      <div className="bg-green-800 px-5 pt-5 pb-16">
        <div className="max-w-lg mx-auto">
          <div className="flex items-center justify-between mb-4">
            <button onClick={() => router.back()} className="text-white p-1">
              <ArrowLeft className="w-5 h-5" />
            </button>
            <div className="flex gap-2">
              <Link href={`/animais/novo?edit=${animal.id}`}
                className="bg-white/20 text-white text-xs font-bold px-3 py-1.5 rounded-xl flex items-center gap-1">
                <Pencil className="w-3.5 h-3.5" /> Editar
              </Link>
              <button onClick={() => setConfirmDelete(true)}
                className="bg-red-500/80 text-white text-xs font-bold px-3 py-1.5 rounded-xl flex items-center gap-1">
                <Trash2 className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="w-16 h-16 bg-white/20 rounded-2xl flex items-center justify-center text-4xl">
              {ESPECIE_EMOJI[animal.especie]}
            </div>
            <div>
              <h1 className="text-2xl font-black text-white">{animal.nome}</h1>
              <p className="text-green-200 text-sm">{animal.raca} · {ESPECIE_LABEL[animal.especie]}</p>
              <div className="flex gap-2 mt-1">
                <span className="bg-white/20 text-white text-[10px] font-bold px-2 py-0.5 rounded-full">
                  {animal.sexo === 'femea' ? '♀ Fêmea' : '♂ Macho'}
                </span>
                {animal.prenhou && (
                  <span className="bg-pink-400/80 text-white text-[10px] font-bold px-2 py-0.5 rounded-full">
                    Gestante
                  </span>
                )}
                {animal.disponivel_match && (
                  <span className="bg-white/20 text-white text-[10px] font-bold px-2 py-0.5 rounded-full flex items-center gap-1">
                    <Dna className="w-3 h-3" /> GeneMatch
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Cards */}
      <div className="max-w-lg mx-auto px-4 -mt-8 space-y-4 pb-8">
        {/* Dados gerais */}
        <div className="bg-white rounded-2xl p-4 shadow-sm">
          <h2 className="font-bold text-gray-900 mb-2 text-sm">Dados gerais</h2>
          <InfoRow label="Fazenda" value={animal.fazenda} />
          <InfoRow label="Nascimento" value={formatDate(animal.data_nascimento)} />
          <InfoRow label="Idade" value={idadeAnimal(animal.data_nascimento)} />
          <InfoRow label="Peso" value={animal.peso_kg ? `${animal.peso_kg} kg` : null} />
          <InfoRow label="Escore corporal" value={animal.escore_corporal ? `${animal.escore_corporal} / 5` : null} />
        </div>

        {/* Reprodutivo (fêmea) */}
        {animal.sexo === 'femea' && (
          <div className="bg-white rounded-2xl p-4 shadow-sm">
            <h2 className="font-bold text-gray-900 mb-2 text-sm flex items-center gap-2">
              <Heart className="w-4 h-4 text-red-500" /> Histórico reprodutivo
            </h2>
            <InfoRow label="Partos" value={animal.numero_partos} />
            <InfoRow label="Filhos nascidos" value={animal.filhos_matriz} />
            <InfoRow label="Abortos" value={animal.abortos} />
            <InfoRow label="Dias pós-parto" value={animal.dias_ultimo_parto || null} />
            {(animal.producao_leite_diaria ?? 0) > 0 && (
              <InfoRow label="Produção de leite" value={`${animal.producao_leite_diaria} L/dia`} />
            )}
          </div>
        )}

        {/* Reprodutivo (macho) */}
        {animal.sexo === 'macho' && (
          <div className="bg-white rounded-2xl p-4 shadow-sm">
            <h2 className="font-bold text-gray-900 mb-2 text-sm">Reprodutor</h2>
            <InfoRow label="Qualidade seminal" value={`${animal.qualidade_semen} / 5`} />
            <InfoRow label="Filhos nascidos" value={animal.filhos_macho} />
          </div>
        )}

        {/* Pedigree */}
        {(animal.nome_pai || animal.nome_mae) && (
          <div className="bg-white rounded-2xl p-4 shadow-sm">
            <h2 className="font-bold text-gray-900 mb-2 text-sm">Pedigree</h2>
            {animal.nome_pai && <InfoRow label="Pai" value={`${animal.nome_pai} (${animal.raca_pai})`} />}
            {animal.nome_mae && <InfoRow label="Mãe" value={`${animal.nome_mae} (${animal.raca_mae})`} />}
          </div>
        )}
      </div>

      {/* Delete confirm */}
      {confirmDelete && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-end justify-center p-4">
          <div className="bg-white rounded-2xl p-6 w-full max-w-sm">
            <h3 className="font-bold text-gray-900 mb-2">Excluir {animal.nome}?</h3>
            <p className="text-gray-500 text-sm mb-4">Esta ação não pode ser desfeita.</p>
            <div className="flex gap-3">
              <button onClick={() => setConfirmDelete(false)}
                className="flex-1 py-3 border border-gray-200 rounded-xl text-sm font-medium">
                Cancelar
              </button>
              <button onClick={handleDelete}
                className="flex-1 py-3 bg-red-500 text-white rounded-xl text-sm font-bold">
                Excluir
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
