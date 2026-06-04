'use client'
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { createClient } from '@/lib/supabase/client'
import type { Animal } from '@/types'
import { ESPECIE_EMOJI } from '@/types'
import { ArrowLeft, CheckCircle2 } from '@/components/Icons'

interface Resultado {
  percentual: number
  predictedClass: number
}

function idadeAnos(data?: string): number {
  if (!data) return 3
  return (Date.now() - new Date(data).getTime()) / (365.25 * 24 * 3600 * 1000)
}

export default function PrenhezPage() {
  const router = useRouter()
  const supabase = createClient()
  const [animais, setAnimais] = useState<Animal[]>([])
  const [matrizId, setMatrizId] = useState('')
  const [machoId, setMachoId]   = useState('')
  const [resultado, setResultado] = useState<Resultado | null>(null)
  const [calculando, setCalculando] = useState(false)
  const [erro, setErro] = useState('')

  useEffect(() => {
    async function load() {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) { router.push('/login'); return }
      const { data: prod } = await supabase.from('genia_produtor').select('id').eq('user_id', user.id).single()
      if (!prod) return
      const { data } = await supabase.from('genia_animal').select('*').eq('produtor_id', prod.id)
      setAnimais(data ?? [])
    }
    load()
  }, [])

  const matrizes = animais.filter(a => a.sexo === 'femea')
  const machos   = animais.filter(a => a.sexo === 'macho')
  const matriz   = animais.find(a => a.id === matrizId)
  const macho    = animais.find(a => a.id === machoId)

  async function calcular() {
    if (!matriz || !macho) return
    setCalculando(true); setErro(''); setResultado(null)

    const payload = {
      especie:                  matriz.especie.charAt(0).toUpperCase() + matriz.especie.slice(1),
      raca_matriz:              matriz.raca,
      idade_matriz:             idadeAnos(matriz.data_nascimento ?? undefined),
      peso_matriz_kg:           matriz.peso_kg ?? 400,
      ecc_matriz:               matriz.escore_corporal ?? 3,
      numero_partos_matriz:     matriz.numero_partos,
      abortos_matriz:           matriz.abortos,
      dias_desde_ultimo_parto:  matriz.dias_ultimo_parto,
      filhos_nascidos_matriz:   matriz.filhos_matriz,
      raca_macho:               macho.raca,
      idade_macho:              idadeAnos(macho.data_nascimento ?? undefined),
      peso_macho_kg:            macho.peso_kg ?? 500,
      qualidade_semen_macho:    macho.qualidade_semen,
      filhos_nascidos_macho:    macho.filhos_macho,
      parentesco_endogamia:     0.05,
    }

    try {
      const res = await fetch('/api/prenhez', {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(payload),
      })
      const data = await res.json()
      if (data.error) throw new Error(data.error)
      setResultado(data)
    } catch (e: any) {
      setErro(e.message ?? 'Erro ao calcular.')
    }
    setCalculando(false)
  }

  const corResultado = !resultado ? '' :
    resultado.percentual >= 70 ? 'text-green-700' :
    resultado.percentual >= 50 ? 'text-amber-600' : 'text-red-600'

  const labelResultado = !resultado ? '' :
    resultado.percentual >= 70 ? 'Alta chance de prenhez' :
    resultado.percentual >= 50 ? 'Chance moderada' : 'Baixa chance de prenhez'

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white px-5 py-4 border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-lg mx-auto flex items-center gap-3">
          <button onClick={() => router.back()}>
            <ArrowLeft className="w-5 h-5 text-gray-600" />
          </button>
          <div>
            <h1 className="text-lg font-bold text-gray-900">Simulação de Prenhez</h1>
            <p className="text-xs text-gray-500">IA preditiva — modelo Random Forest</p>
          </div>
        </div>
      </div>

      <div className="max-w-lg mx-auto px-4 py-5 space-y-4">
        {/* Seleção de Matriz */}
        <div className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm">
          <p className="text-sm font-bold text-pink-700 mb-2">♀ Selecione a Matriz (Fêmea)</p>
          {matrizes.length === 0 ? (
            <p className="text-sm text-gray-400">Nenhuma fêmea cadastrada.</p>
          ) : (
            <div className="space-y-2">
              {matrizes.map(a => (
                <button key={a.id} onClick={() => setMatrizId(a.id)}
                  className={`w-full flex items-center gap-3 p-3 rounded-xl border transition ${
                    matrizId === a.id ? 'border-pink-500 bg-pink-50' : 'border-gray-200 bg-white'
                  }`}>
                  <span className="text-xl">{ESPECIE_EMOJI[a.especie]}</span>
                  <div className="flex-1 text-left">
                    <p className="text-sm font-medium text-gray-900">{a.nome}</p>
                    <p className="text-xs text-gray-500">{a.raca} · {a.numero_partos} partos · ECC {a.escore_corporal ?? 3}</p>
                  </div>
                  {matrizId === a.id && <CheckCircle2 className="w-5 h-5 text-pink-600" />}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Seleção de Macho */}
        <div className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm">
          <p className="text-sm font-bold text-blue-700 mb-2">♂ Selecione o Reprodutor (Macho)</p>
          {machos.length === 0 ? (
            <p className="text-sm text-gray-400">Nenhum macho cadastrado.</p>
          ) : (
            <div className="space-y-2">
              {machos.map(a => (
                <button key={a.id} onClick={() => setMachoId(a.id)}
                  className={`w-full flex items-center gap-3 p-3 rounded-xl border transition ${
                    machoId === a.id ? 'border-blue-500 bg-blue-50' : 'border-gray-200 bg-white'
                  }`}>
                  <span className="text-xl">{ESPECIE_EMOJI[a.especie]}</span>
                  <div className="flex-1 text-left">
                    <p className="text-sm font-medium text-gray-900">{a.nome}</p>
                    <p className="text-xs text-gray-500">{a.raca} · Sêmen {a.qualidade_semen}/5 · {a.filhos_macho} filhos</p>
                  </div>
                  {machoId === a.id && <CheckCircle2 className="w-5 h-5 text-blue-600" />}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Resultado */}
        {resultado && (
          <div className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm text-center">
            <p className="text-xs text-gray-500 mb-2">Taxa de Prenhez Estimada</p>
            <p className={`text-7xl font-extrabold ${corResultado}`}>{resultado.percentual}%</p>
            <p className={`text-lg font-bold mt-1 ${corResultado}`}>{labelResultado}</p>
            <div className="mt-4 pt-4 border-t border-gray-50 text-sm text-gray-500 space-y-1">
              <p>♀ Matriz: <strong className="text-gray-900">{matriz?.nome}</strong></p>
              <p>♂ Reprodutor: <strong className="text-gray-900">{macho?.nome}</strong></p>
            </div>
            <div className="mt-3 bg-blue-50 rounded-xl p-3 flex gap-2 text-left">
              <span className="text-blue-600 text-sm shrink-0">ℹ️</span>
              <p className="text-xs text-blue-700">
                Estimativa baseada em modelo Random Forest treinado com dados reprodutivos e genéticos
                — mesma IA embarcada no aplicativo Android.
              </p>
            </div>
          </div>
        )}

        {erro && (
          <div className="bg-red-50 border border-red-200 rounded-2xl p-4 text-red-700 text-sm">{erro}</div>
        )}

        <button
          onClick={calcular}
          disabled={!matrizId || !machoId || calculando}
          className="w-full py-4 bg-green-800 text-white font-bold rounded-2xl disabled:opacity-50 transition hover:bg-green-700 flex items-center justify-center gap-2"
        >
          {calculando ? (
            <><span className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" /> Calculando...</>
          ) : (
            'CALCULAR PRENHEZ'
          )}
        </button>

        <p className="text-center text-xs text-gray-400 pb-4">
          Modelo Random Forest · {animais.length} animais analisados
        </p>
      </div>
    </div>
  )
}
