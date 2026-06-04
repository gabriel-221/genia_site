'use client'
import { useEffect, useState } from 'react'
import { createClient } from '@/lib/supabase/client'
import type { Animal, Especie } from '@/types'
import { ESPECIE_EMOJI, ESPECIE_LABEL } from '@/types'
import { ArrowLeft } from '@/components/Icons'
import { useRouter } from 'next/navigation'

function exportarCSV(animais: Animal[]) {
  const cabecalho = [
    'Nome','Espécie','Raça','Sexo','Nascimento','Peso (kg)','ECC','Fazenda',
    'Partos','Abortos','Filhos','Prod. Leite (L/dia)','Qld. Sêmen','Gestante','GeneMatch'
  ].join(';')

  const linhas = animais.map(a => [
    a.nome, a.especie, a.raca, a.sexo,
    a.data_nascimento ?? '',
    a.peso_kg ?? '',
    a.escore_corporal ?? '',
    a.fazenda,
    a.numero_partos,
    a.abortos,
    a.filhos_matriz || a.filhos_macho,
    a.producao_leite_diaria ?? 0,
    a.qualidade_semen ?? '',
    a.prenhou ? 'Sim' : 'Não',
    a.disponivel_match ? 'Sim' : 'Não',
  ].join(';')).join('\n')

  const blob = new Blob(['﻿' + cabecalho + '\n' + linhas], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `genia_rebanho_${new Date().toISOString().slice(0,10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

function KpiCard({ titulo, valor, status, cor }: { titulo: string; valor: string; status: string; cor: string }) {
  return (
    <div className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm flex-1">
      <p className="text-xs text-gray-500 mb-1">{titulo}</p>
      <p className={`text-3xl font-extrabold ${cor}`}>{valor}</p>
      <p className={`text-xs font-bold mt-0.5 ${cor} opacity-80`}>{status}</p>
    </div>
  )
}

function MetricRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between py-2.5 border-b border-gray-50 last:border-0">
      <span className="text-sm text-gray-500">{label}</span>
      <span className="text-sm font-bold text-gray-900">{value}</span>
    </div>
  )
}

export default function RelatoriosPage() {
  const router = useRouter()
  const supabase = createClient()
  const [animais, setAnimais] = useState<Animal[]>([])
  const [filtro, setFiltro] = useState<Especie | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) return
      const { data: prod } = await supabase.from('genia_produtor').select('id').eq('user_id', user.id).single()
      if (!prod) { setLoading(false); return }
      const { data } = await supabase.from('genia_animal').select('*').eq('produtor_id', prod.id)
      setAnimais(data ?? [])
      setLoading(false)
    }
    load()
  }, [])

  const todos = filtro ? animais.filter(a => a.especie === filtro) : animais
  const total  = todos.length
  const femeas = todos.filter(a => a.sexo === 'femea')
  const machos = todos.filter(a => a.sexo === 'macho')
  const prenhas = todos.filter(a => a.prenhou)
  const taxaPrenhez = femeas.length > 0 ? Math.round((prenhas.length / femeas.length) * 100) : 0
  const dispMatch = todos.filter(a => a.disponivel_match).length
  const mediaLeite = femeas.filter(a => (a.producao_leite_diaria ?? 0) > 0)
  const mediaLeiteFinal = mediaLeite.length > 0
    ? (mediaLeite.reduce((s, a) => s + (a.producao_leite_diaria ?? 0), 0) / mediaLeite.length).toFixed(1)
    : null
  const mediaPeso = todos.filter(a => a.peso_kg).length > 0
    ? Math.round(todos.filter(a => a.peso_kg).reduce((s, a) => s + (a.peso_kg ?? 0), 0) / todos.filter(a => a.peso_kg).length)
    : null
  const mediaPartos = femeas.length > 0
    ? (femeas.reduce((s, a) => s + a.numero_partos, 0) / femeas.length).toFixed(1)
    : null
  const mediaQldSemen = machos.length > 0
    ? (machos.reduce((s, a) => s + a.qualidade_semen, 0) / machos.length).toFixed(1)
    : null

  if (loading) return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="w-8 h-8 border-2 border-green-800 border-t-transparent rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white px-5 py-4 border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-lg mx-auto flex items-center gap-3">
          <button onClick={() => router.back()}>
            <ArrowLeft className="w-5 h-5 text-gray-600" />
          </button>
          <h1 className="text-xl font-bold text-gray-900 flex-1">Relatórios</h1>
          {animais.length > 0 && (
            <button
              onClick={() => exportarCSV(animais)}
              className="bg-green-800 text-white text-xs font-bold px-3 py-1.5 rounded-xl"
              title="Exportar dados do rebanho em CSV"
            >
              Exportar CSV
            </button>
          )}
        </div>
      </div>

      <div className="max-w-lg mx-auto px-4 py-4 space-y-4">
        {/* Filtro */}
        <div>
          <p className="text-xs font-semibold text-gray-500 mb-2">Filtrar por espécie</p>
          <div className="flex gap-2 flex-wrap">
            {[null, 'bovino', 'ovino', 'caprino'].map(esp => (
              <button key={esp ?? 'all'} onClick={() => setFiltro(esp as Especie | null)}
                className={`px-3 py-1.5 rounded-xl text-xs font-bold transition ${
                  filtro === esp ? 'bg-green-800 text-white' : 'bg-white text-gray-600 border border-gray-200'
                }`}>
                {esp ? `${ESPECIE_EMOJI[esp as Especie]} ${ESPECIE_LABEL[esp as Especie]}` : 'Todos'}
              </button>
            ))}
          </div>
        </div>

        {total === 0 ? (
          <div className="bg-white rounded-2xl p-12 text-center border border-gray-100">
            <p className="text-4xl mb-3">📊</p>
            <p className="text-gray-500">
              {filtro ? `Nenhum ${ESPECIE_LABEL[filtro]} cadastrado.` : 'Nenhum animal cadastrado.'}
            </p>
          </div>
        ) : (
          <>
            {/* KPIs */}
            <div className="flex gap-3">
              <KpiCard
                titulo="Taxa de Prenhez"
                valor={femeas.length > 0 ? `${taxaPrenhez}%` : '—'}
                status={taxaPrenhez >= 70 ? 'Excelente' : taxaPrenhez >= 50 ? 'Bom' : 'Melhorar'}
                cor={taxaPrenhez >= 70 ? 'text-green-800' : taxaPrenhez >= 50 ? 'text-amber-600' : 'text-red-600'}
              />
              <KpiCard
                titulo="Total de Animais"
                valor={`${total}`}
                status={`${femeas.length}♀ · ${machos.length}♂`}
                cor="text-blue-700"
              />
            </div>

            {/* Reprodução */}
            <div className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm">
              <h3 className="font-bold text-sm text-gray-900 mb-3">Reprodução</h3>
              <MetricRow label="Total de animais" value={`${total}`} />
              <MetricRow label="Fêmeas" value={`${femeas.length}`} />
              <MetricRow label="Em gestação" value={`${prenhas.length}`} />
              {mediaPartos && <MetricRow label="Média de partos (fêmeas)" value={`${mediaPartos}`} />}
              <MetricRow label="Disponíveis no GeneMatch" value={`${dispMatch}`} />
            </div>

            {/* Genética */}
            <div className="bg-white rounded-2xl p-4 border border-gray-100 shadow-sm">
              <h3 className="font-bold text-sm text-gray-900 mb-3">Genética &amp; Produção</h3>
              {mediaPeso && <MetricRow label="Peso médio" value={`${mediaPeso} kg`} />}
              {mediaQldSemen && <MetricRow label="Qualidade seminal média" value={`${mediaQldSemen} / 5`} />}
              {mediaLeiteFinal && <MetricRow label="Produção de leite média" value={`${mediaLeiteFinal} L/dia`} />}
            </div>

            {/* Dica dinâmica */}
            <div className="bg-green-50 rounded-2xl p-4 flex gap-3">
              <span className="text-xl shrink-0">💡</span>
              <p className="text-sm text-green-900 leading-relaxed">
                {femeas.length === 0
                  ? 'Cadastre fêmeas para obter análises de prenhez e recomendações de cruzamento.'
                  : taxaPrenhez < 50
                  ? 'Taxa de prenhez abaixo de 50%. Use o GeneMatch para selecionar melhores reprodutores.'
                  : taxaPrenhez < 70
                  ? 'Taxa pode melhorar. Verifique o escore corporal das fêmeas e o intervalo entre partos.'
                  : 'Rebanho com ótimo desempenho reprodutivo! Use o Copilot para recomendações avançadas.'}
              </p>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
