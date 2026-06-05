import { supabase } from '@/lib/supabase'
import HeroSection from '@/components/HeroSection'
import StatsCard from '@/components/StatsCard'
import FamilyChain from '@/components/FamilyChain'
import ReproductionCard from '@/components/ReproductionCard'
import EventTimeline from '@/components/EventTimeline'
import OwnerCard from '@/components/OwnerCard'
import AnimalNotFound from '@/components/AnimalNotFound'
import type { Animal, EventoReprodutivo } from '@/types'

type AnimalSlim = Pick<Animal, 'id' | 'nome' | 'especie' | 'sexo'>

export default async function AnimalPage({
  params,
}: {
  params: { nfcCode: string }
}) {
  const code = params.nfcCode

  const { data: animal } = (await supabase
    .from('genia_animal')
    .select('*, genia_produtor(nome, nome_fazenda, municipio, estado)')
    .eq('id', code)
    .eq('ativo', true)
    .single()) as { data: Animal | null }

  if (!animal) return <AnimalNotFound code={code} />

  // Parallel: pedigree + filhos + eventos (all animals)
  const [{ data: pedigree }, { data: filhosRaw }, { data: eventosRaw }] = await Promise.all([
    supabase.from('genia_pedigree').select('*').eq('animal_id', animal.id).single(),
    supabase
      .from('genia_pedigree')
      .select('animal_id, genia_animal(id, nome, especie, sexo)')
      .or(`pai_id.eq.${animal.id},mae_id.eq.${animal.id}`),
    supabase
      .from('genia_evento_reprodutivo')
      .select('*')
      .eq('animal_id', animal.id)
      .order('data_evento', { ascending: false })
      .limit(5),
  ])

  let pai: AnimalSlim | null = null
  let mae: AnimalSlim | null = null

  if (pedigree?.pai_id) {
    const { data } = await supabase
      .from('genia_animal').select('id, nome, especie, sexo')
      .eq('id', pedigree.pai_id).single()
    pai = data
  }
  if (pedigree?.mae_id) {
    const { data } = await supabase
      .from('genia_animal').select('id, nome, especie, sexo')
      .eq('id', pedigree.mae_id).single()
    mae = data
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const filhos: AnimalSlim[] = (filhosRaw ?? []).map((f: any) => f.genia_animal).filter(Boolean)
  const eventos: EventoReprodutivo[] = (eventosRaw ?? []) as EventoReprodutivo[]

  // Reproductive metrics (females only, from fetched events)
  let lastEvento: EventoReprodutivo | null = null
  let taxaPrenhez: number | null = null

  if (animal.sexo === 'femea' && eventos.length) {
    lastEvento = eventos[0]
    const inseminacoes = eventos.filter(e => e.tipo === 'inseminacao')
    const confirmadas  = inseminacoes.filter(e => e.gestacao_confirmada)
    if (inseminacoes.length > 0) {
      taxaPrenhez = Math.round((confirmadas.length / inseminacoes.length) * 100)
    }
    if (lastEvento?.score_ia_prenhez) {
      taxaPrenhez = Math.round((lastEvento.score_ia_prenhez ?? 0) * 100)
    }
  }

  return (
    <main className="min-h-screen bg-gray-100">
      <HeroSection animal={animal} />

      <div className="relative -mt-8 z-10">
        <div className="max-w-lg mx-auto px-4 space-y-3 pb-12">
          <StatsCard animal={animal} />

          {(pai || mae || filhos.length > 0) && (
            <FamilyChain animal={animal} pai={pai} mae={mae} filhos={filhos} />
          )}

          {animal.sexo === 'femea' && (lastEvento || taxaPrenhez !== null) && (
            <ReproductionCard lastEvento={lastEvento} taxaPrenhez={taxaPrenhez} />
          )}

          {eventos.length > 0 && <EventTimeline eventos={eventos} />}

          {animal.genia_produtor && <OwnerCard produtor={animal.genia_produtor} />}

          <p className="text-center text-xs text-gray-400 pt-2 pb-4">
            Rastreabilidade garantida por G.E.N.I.A · GNU GPL v3.0
          </p>
        </div>
      </div>
    </main>
  )
}
