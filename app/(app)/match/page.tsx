'use client'
import { useEffect, useState } from 'react'
import { createClient } from '@/lib/supabase/client'
import type { Animal, ScoredAnimal } from '@/types'
import { ESPECIE_EMOJI, ESPECIE_LABEL } from '@/types'
import { rankAnimals } from '@/lib/genetic-ranking'
import { Dna, ArrowLeft, CheckCircle2, X, Users } from '@/components/Icons'

interface AnimalComProdutor extends Animal {
  genia_produtor?: {
    nome: string
    nome_fazenda?: string
    municipio: string
    estado: string
  } | null
}

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

function idadeAnos(data?: string) {
  if (!data) return null
  const anos = (Date.now() - new Date(data).getTime()) / (365.25 * 24 * 3600 * 1000)
  if (anos < 1) return `${Math.round(anos * 12)} meses`
  return `${Math.floor(anos)} ${Math.floor(anos) === 1 ? 'ano' : 'anos'}`
}

function formatarWhatsApp(telefone?: string) {
  if (!telefone) return null
  const digits = telefone.replace(/\D/g, '')
  return digits.startsWith('55') ? digits : `55${digits}`
}

export default function MatchPage() {
  const supabase = createClient()
  const [animais, setAnimais] = useState<Animal[]>([])
  const [outrosAnimais, setOutrosAnimais] = useState<AnimalComProdutor[]>([])
  const [objetivo, setObjetivo] = useState('')
  const [ranking, setRanking] = useState<ScoredAnimal[]>([])
  const [step, setStep] = useState<'select' | 'result'>('select')
  const [modalAnimal, setModalAnimal] = useState<AnimalComProdutor | null>(null)

  useEffect(() => {
    async function load() {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) return

      const { data: prod } = await supabase
        .from('genia_produtor').select('id').eq('user_id', user.id).single()
      if (!prod) return

      // Meus animais para o ranking
      const { data: meusAnim } = await supabase
        .from('genia_animal').select('*').eq('produtor_id', prod.id)
      setAnimais(meusAnim ?? [])

      // Animais de OUTROS produtores disponíveis para match (telefone carregado só ao contatar)
      const { data: outros } = await supabase
        .from('genia_animal')
        .select('*, genia_produtor(nome, nome_fazenda, municipio, estado)')
        .eq('disponivel_match', true)
        .neq('produtor_id', prod.id)
        .limit(30)
      setOutrosAnimais(outros ?? [])
    }
    load()
  }, [])

  function buscar() {
    if (!objetivo) return
    setRanking(rankAnimals(objetivo, animais))
    setStep('result')
  }

  async function abrirWhatsApp(animal: AnimalComProdutor) {
    // Fetch phone only on demand — not bulk-loaded with all animals
    const { data: prod } = await supabase
      .from('genia_produtor')
      .select('telefone')
      .eq('id', animal.produtor_id)
      .single()

    const phone = formatarWhatsApp(prod?.telefone)
    if (!phone) {
      alert('Número de WhatsApp não disponível para este produtor.')
      return
    }
    const nome = animal.genia_produtor?.nome ?? 'Produtor'
    const msg = `Olá, ${nome}! Vi o animal *${animal.nome}* (${animal.raca} - ${ESPECIE_LABEL[animal.especie]}) disponível para GeneMatch no *GENIA* e tenho interesse em discutir uma parceria de melhoramento genético. Podemos conversar? 🐄🌿`
    window.open(`https://wa.me/${phone}?text=${encodeURIComponent(msg)}`, '_blank')
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
            <p className="text-xs text-gray-500">Ranqueamento + rede de produtores</p>
          </div>
        </div>
      </div>

      <div className="max-w-lg mx-auto px-4 py-5 space-y-5">
        {step === 'select' && (
          <>
            {/* Meu plantel — ranking */}
            <div>
              <h2 className="text-sm font-bold text-gray-700 mb-3 flex items-center gap-2">
                <span className="w-5 h-5 bg-green-800 text-white rounded-full flex items-center justify-center text-[10px] font-bold">1</span>
                Ranquear meu plantel por objetivo
              </h2>
              <div className="grid gap-3">
                {OBJETIVOS.map(o => (
                  <button key={o.key} onClick={() => setObjetivo(o.key)}
                    className={`w-full p-4 rounded-2xl border-2 text-left transition ${
                      objetivo === o.key ? 'border-green-800 bg-green-50' : 'border-gray-200 bg-white'
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

              <button onClick={buscar} disabled={!objetivo || animais.length === 0}
                className="w-full mt-3 py-3.5 bg-green-800 text-white font-bold rounded-xl disabled:opacity-50 hover:bg-green-700 transition">
                {animais.length === 0 ? 'Nenhum animal cadastrado' : 'RANQUEAR MEU PLANTEL'}
              </button>
            </div>

            {/* Animais de outros produtores */}
            {outrosAnimais.length > 0 && (
              <div>
                <h2 className="text-sm font-bold text-gray-700 mb-3 flex items-center gap-2">
                  <span className="w-5 h-5 bg-blue-600 text-white rounded-full flex items-center justify-center text-[10px] font-bold">2</span>
                  Animais disponíveis de outros produtores
                  <span className="ml-auto text-xs text-gray-400">{outrosAnimais.length} animal{outrosAnimais.length !== 1 ? 'is' : ''}</span>
                </h2>

                <div className="space-y-2">
                  {outrosAnimais.map(a => (
                    <button key={a.id} onClick={() => setModalAnimal(a)}
                      className="w-full bg-white rounded-2xl p-4 border border-gray-100 shadow-sm flex items-center gap-3 text-left hover:border-green-300 transition active:bg-gray-50">
                      <div className="w-12 h-12 bg-green-100 rounded-2xl flex items-center justify-center text-2xl shrink-0">
                        {ESPECIE_EMOJI[a.especie]}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="font-bold text-gray-900">{a.nome}</p>
                        <p className="text-xs text-gray-500">{a.raca} · {ESPECIE_LABEL[a.especie]}</p>
                        <p className="text-xs text-green-700 font-medium mt-0.5">
                          {a.genia_produtor?.nome_fazenda ?? a.genia_produtor?.municipio ?? ''} — {a.genia_produtor?.municipio}
                        </p>
                      </div>
                      <div className="text-right shrink-0">
                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                          a.sexo === 'femea' ? 'bg-pink-50 text-pink-600' : 'bg-blue-50 text-blue-600'
                        }`}>
                          {a.sexo === 'femea' ? '♀' : '♂'}
                        </span>
                        {a.peso_kg && <p className="text-[10px] text-gray-400 mt-1">{a.peso_kg}kg</p>}
                      </div>
                    </button>
                  ))}
                </div>
              </div>
            )}

            {outrosAnimais.length === 0 && animais.length > 0 && (
              <div className="bg-blue-50 rounded-2xl p-4 text-center">
                <Users className="w-8 h-8 text-blue-400 mx-auto mb-2" />
                <p className="text-sm text-blue-700 font-medium">Nenhum animal de outros produtores disponível</p>
                <p className="text-xs text-blue-500 mt-1">Quando outros produtores marcarem seus animais para GeneMatch, eles aparecerão aqui.</p>
              </div>
            )}
          </>
        )}

        {/* Resultado do ranking */}
        {step === 'result' && (
          <div className="space-y-3">
            <p className="text-sm text-gray-500">
              Objetivo: <strong>{OBJETIVOS.find(o => o.key === objetivo)?.label}</strong> · {ranking.length} animais ranqueados
            </p>
            {ranking.length === 0 ? (
              <div className="text-center py-16 text-gray-500">Nenhum animal para ranquear.</div>
            ) : ranking.map((item, idx) => (
              <div key={item.animal.id} className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm flex items-start gap-3">
                <div className={`w-9 h-9 rounded-full flex items-center justify-center font-extrabold text-sm shrink-0 ${
                  idx === 0 ? 'bg-yellow-400 text-yellow-900' :
                  idx === 1 ? 'bg-gray-200 text-gray-600' :
                  idx === 2 ? 'bg-orange-200 text-orange-700' : 'bg-gray-100 text-gray-500'
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

      {/* Modal — detalhe do animal de outro produtor */}
      {modalAnimal && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-end justify-center p-4"
          onClick={() => setModalAnimal(null)}>
          <div className="bg-white rounded-3xl w-full max-w-sm p-6 shadow-2xl"
            onClick={e => e.stopPropagation()}>

            {/* Header */}
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <span className="text-3xl">{ESPECIE_EMOJI[modalAnimal.especie]}</span>
                <div>
                  <h3 className="font-black text-gray-900 text-lg">{modalAnimal.nome}</h3>
                  <p className="text-xs text-gray-500">{modalAnimal.raca} · {ESPECIE_LABEL[modalAnimal.especie]}</p>
                </div>
              </div>
              <button onClick={() => setModalAnimal(null)} className="text-gray-400 p-1">
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Info do animal */}
            <div className="space-y-2 mb-4">
              <div className="flex justify-between text-sm border-b border-gray-50 pb-2">
                <span className="text-gray-500">Sexo</span>
                <span className="font-medium">{modalAnimal.sexo === 'femea' ? '♀ Fêmea' : '♂ Macho'}</span>
              </div>
              {modalAnimal.data_nascimento && (
                <div className="flex justify-between text-sm border-b border-gray-50 pb-2">
                  <span className="text-gray-500">Idade</span>
                  <span className="font-medium">{idadeAnos(modalAnimal.data_nascimento)}</span>
                </div>
              )}
              {modalAnimal.peso_kg && (
                <div className="flex justify-between text-sm border-b border-gray-50 pb-2">
                  <span className="text-gray-500">Peso</span>
                  <span className="font-medium">{modalAnimal.peso_kg} kg</span>
                </div>
              )}
              {modalAnimal.escore_corporal && (
                <div className="flex justify-between text-sm border-b border-gray-50 pb-2">
                  <span className="text-gray-500">ECC</span>
                  <span className="font-medium">{modalAnimal.escore_corporal} / 5</span>
                </div>
              )}
              {modalAnimal.sexo === 'femea' && (
                <>
                  <div className="flex justify-between text-sm border-b border-gray-50 pb-2">
                    <span className="text-gray-500">Partos</span>
                    <span className="font-medium">{modalAnimal.numero_partos}</span>
                  </div>
                  {(modalAnimal.producao_leite_diaria ?? 0) > 0 && (
                    <div className="flex justify-between text-sm border-b border-gray-50 pb-2">
                      <span className="text-gray-500">Produção leite</span>
                      <span className="font-medium">{modalAnimal.producao_leite_diaria} L/dia</span>
                    </div>
                  )}
                </>
              )}
              {modalAnimal.sexo === 'macho' && (
                <div className="flex justify-between text-sm border-b border-gray-50 pb-2">
                  <span className="text-gray-500">Qualidade seminal</span>
                  <span className="font-medium">{modalAnimal.qualidade_semen} / 5</span>
                </div>
              )}
            </div>

            {/* Info do produtor */}
            {modalAnimal.genia_produtor && (
              <div className="bg-green-50 rounded-xl p-3 mb-4">
                <p className="text-xs font-bold text-green-800 mb-1">Produtor</p>
                <p className="text-sm font-medium text-gray-900">{modalAnimal.genia_produtor.nome}</p>
                <p className="text-xs text-gray-600">
                  {modalAnimal.genia_produtor.nome_fazenda && `${modalAnimal.genia_produtor.nome_fazenda} · `}
                  {modalAnimal.genia_produtor.municipio}, {modalAnimal.genia_produtor.estado}
                </p>
              </div>
            )}

            {/* Botões */}
            <div className="space-y-2">
              <button
                onClick={() => abrirWhatsApp(modalAnimal)}
                className="w-full py-3.5 bg-[#25D366] text-white font-bold rounded-xl flex items-center justify-center gap-2 hover:bg-[#20bd59] transition"
              >
                <svg viewBox="0 0 24 24" className="w-5 h-5 fill-white" xmlns="http://www.w3.org/2000/svg">
                  <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413Z"/>
                </svg>
                Entrar em contato via WhatsApp
              </button>
              <button onClick={() => setModalAnimal(null)}
                className="w-full py-3 border border-gray-200 rounded-xl text-sm text-gray-600 font-medium">
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
