'use client'
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { createClient } from '@/lib/supabase/client'
import type { Especie, Sexo } from '@/types'
import { RACAS, ESPECIE_EMOJI, ESPECIE_LABEL } from '@/types'
import { ArrowLeft, Check, ChevronRight } from '@/components/Icons'

interface Form {
  nome: string; especie: Especie; sexo: Sexo; raca: string
  dataNascimento: string; peso: string; escore: string; fazenda: string
  disponivelMatch: boolean
  nomePai: string; racaPai: string; nomeMae: string; racaMae: string
  partos: string; abortos: string; diasPosParto: string; filhosMatriz: string; producaoLeite: string
  qualidadeSemen: string; filhosMacho: string
}

const defaultForm: Form = {
  nome: '', especie: 'bovino', sexo: 'femea', raca: '', dataNascimento: '',
  peso: '', escore: '3', fazenda: '', disponivelMatch: true,
  nomePai: '', racaPai: '', nomeMae: '', racaMae: '',
  partos: '0', abortos: '0', diasPosParto: '0', filhosMatriz: '0', producaoLeite: '0',
  qualidadeSemen: '3', filhosMacho: '0',
}

function Label({ children }: { children: React.ReactNode }) {
  return <label className="text-xs font-semibold text-gray-600 mb-1 block">{children}</label>
}

function Input({ value, onChange, type = 'text', placeholder = '' }: {
  value: string; onChange: (v: string) => void; type?: string; placeholder?: string
}) {
  return (
    <input
      type={type}
      value={value}
      onChange={e => onChange(e.target.value)}
      placeholder={placeholder}
      className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-green-600 focus:ring-1 focus:ring-green-600 bg-gray-50"
    />
  )
}

export default function NovoAnimalPage() {
  const router = useRouter()
  const supabase = createClient()
  const [passo, setPasso] = useState(1)
  const [form, setForm] = useState<Form>(defaultForm)
  const [saving, setSaving] = useState(false)
  const [produtorId, setProdutorId] = useState<string | null>(null)
  const [erro, setErro] = useState('')

  useEffect(() => {
    supabase.auth.getUser().then(({ data: { user } }) => {
      if (!user) return
      supabase.from('genia_produtor').select('id').eq('user_id', user.id).single()
        .then(({ data }) => setProdutorId(data?.id ?? null))
    })
  }, [])

  function set(key: keyof Form, value: string | boolean) {
    setForm(f => ({ ...f, [key]: value }))
    if (key === 'especie') setForm(f => ({ ...f, especie: value as Especie, raca: '' }))
  }

  async function salvar() {
    if (!produtorId) { setErro('Produtor não identificado.'); return }
    if (!form.nome || !form.raca) { setErro('Preencha nome e raça.'); return }
    setSaving(true); setErro('')

    const payload = {
      produtor_id: produtorId,
      nome: form.nome.trim(),
      especie: form.especie,
      sexo: form.sexo,
      raca: form.raca,
      data_nascimento: form.dataNascimento || null,
      peso_kg: parseFloat(form.peso) || null,
      escore_corporal: parseFloat(form.escore) || 3,
      fazenda: form.fazenda.trim(),
      disponivel_match: form.disponivelMatch,
      nome_pai: form.nomePai, raca_pai: form.racaPai,
      nome_mae: form.nomeMae, raca_mae: form.racaMae,
      numero_partos: parseInt(form.partos) || 0,
      abortos: parseInt(form.abortos) || 0,
      dias_ultimo_parto: parseInt(form.diasPosParto) || 0,
      filhos_matriz: parseInt(form.filhosMatriz) || 0,
      producao_leite_diaria: parseFloat(form.producaoLeite) || 0,
      qualidade_semen: parseFloat(form.qualidadeSemen) || 3,
      filhos_macho: parseInt(form.filhosMacho) || 0,
      prenhou: false, ativo: true,
    }

    const { error } = await supabase.from('genia_animal').insert(payload)
    setSaving(false)
    if (error) { setErro('Erro ao salvar: ' + error.message) }
    else { router.push('/animais') }
  }

  const racas = RACAS[form.especie]

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white px-5 py-4 border-b border-gray-100 sticky top-0 z-10">
        <div className="max-w-lg mx-auto flex items-center gap-3">
          <button onClick={() => passo > 1 ? setPasso(p => p - 1) : router.back()}>
            <ArrowLeft className="w-5 h-5 text-gray-600" />
          </button>
          <div className="flex-1">
            <p className="font-bold text-gray-900">
              {passo === 1 ? 'Informações' : passo === 2 ? 'Pedigree' : 'Revisão'}
            </p>
            <div className="flex gap-1 mt-1">
              {[1,2,3].map(n => (
                <div key={n} className={`h-1 flex-1 rounded-full ${n <= passo ? 'bg-green-800' : 'bg-gray-200'}`} />
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-lg mx-auto px-4 py-5">
        {passo === 1 && (
          <div className="space-y-4">
            <div>
              <Label>Nome do animal *</Label>
              <Input value={form.nome} onChange={v => set('nome', v)} placeholder="Ex: Frida" />
            </div>

            {/* Espécie */}
            <div>
              <Label>Espécie *</Label>
              <div className="flex gap-2">
                {(['bovino', 'ovino', 'caprino'] as Especie[]).map(esp => (
                  <button key={esp} onClick={() => set('especie', esp)}
                    className={`flex-1 py-2.5 rounded-xl border text-sm font-medium transition ${
                      form.especie === esp ? 'bg-green-800 border-green-800 text-white' : 'bg-white border-gray-200 text-gray-600'
                    }`}>
                    {ESPECIE_EMOJI[esp]} {ESPECIE_LABEL[esp]}
                  </button>
                ))}
              </div>
            </div>

            {/* Sexo */}
            <div>
              <Label>Sexo *</Label>
              <div className="flex gap-2">
                {(['femea', 'macho'] as Sexo[]).map(s => (
                  <button key={s} onClick={() => set('sexo', s)}
                    className={`flex-1 py-2.5 rounded-xl border text-sm font-medium transition ${
                      form.sexo === s ? 'bg-green-800 border-green-800 text-white' : 'bg-white border-gray-200 text-gray-600'
                    }`}>
                    {s === 'femea' ? '♀ Fêmea' : '♂ Macho'}
                  </button>
                ))}
              </div>
            </div>

            {/* Raça */}
            <div>
              <Label>Raça *</Label>
              <div className="flex flex-wrap gap-2">
                {racas.map(r => (
                  <button key={r} onClick={() => set('raca', r)}
                    className={`px-3 py-1.5 rounded-xl border text-sm transition ${
                      form.raca === r ? 'bg-green-800 border-green-800 text-white' : 'bg-white border-gray-200 text-gray-600'
                    }`}>{r}</button>
                ))}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label>Nascimento</Label>
                <Input value={form.dataNascimento} onChange={v => set('dataNascimento', v)} type="date" />
              </div>
              <div>
                <Label>Peso (kg)</Label>
                <Input value={form.peso} onChange={v => set('peso', v)} type="number" placeholder="450" />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label>Escore corporal (1-5)</Label>
                <Input value={form.escore} onChange={v => set('escore', v)} type="number" placeholder="3" />
              </div>
              <div>
                <Label>Fazenda</Label>
                <Input value={form.fazenda} onChange={v => set('fazenda', v)} placeholder="Nome da fazenda" />
              </div>
            </div>

            {/* Fêmea */}
            {form.sexo === 'femea' && (
              <div className="bg-pink-50 rounded-2xl p-4 space-y-3">
                <p className="text-xs font-bold text-pink-700">Histórico da Matriz</p>
                <div className="grid grid-cols-2 gap-3">
                  <div><Label>Partos</Label><Input value={form.partos} onChange={v => set('partos', v)} type="number" /></div>
                  <div><Label>Abortos</Label><Input value={form.abortos} onChange={v => set('abortos', v)} type="number" /></div>
                  <div><Label>Dias pós-parto</Label><Input value={form.diasPosParto} onChange={v => set('diasPosParto', v)} type="number" /></div>
                  <div><Label>Filhos nascidos</Label><Input value={form.filhosMatriz} onChange={v => set('filhosMatriz', v)} type="number" /></div>
                  <div className="col-span-2"><Label>Produção leite (L/dia)</Label><Input value={form.producaoLeite} onChange={v => set('producaoLeite', v)} type="number" placeholder="0" /></div>
                </div>
              </div>
            )}

            {/* Macho */}
            {form.sexo === 'macho' && (
              <div className="bg-blue-50 rounded-2xl p-4 space-y-3">
                <p className="text-xs font-bold text-blue-700">Histórico do Reprodutor</p>
                <div className="grid grid-cols-2 gap-3">
                  <div><Label>Qualidade seminal (1-5)</Label><Input value={form.qualidadeSemen} onChange={v => set('qualidadeSemen', v)} type="number" /></div>
                  <div><Label>Filhos nascidos</Label><Input value={form.filhosMacho} onChange={v => set('filhosMacho', v)} type="number" /></div>
                </div>
              </div>
            )}

            {/* GeneMatch toggle */}
            <div className="flex items-center justify-between bg-white rounded-2xl p-4 border border-gray-100">
              <div>
                <p className="text-sm font-medium text-gray-900">Disponível no GeneMatch</p>
                <p className="text-xs text-gray-500">Outros produtores poderão ver este animal</p>
              </div>
              <button
                onClick={() => set('disponivelMatch', !form.disponivelMatch)}
                className={`w-12 h-6 rounded-full transition-colors ${form.disponivelMatch ? 'bg-green-800' : 'bg-gray-300'}`}
              >
                <div className={`w-5 h-5 bg-white rounded-full shadow transition-transform ${form.disponivelMatch ? 'translate-x-6' : 'translate-x-0.5'}`} />
              </button>
            </div>
          </div>
        )}

        {passo === 2 && (
          <div className="space-y-4">
            <p className="text-sm text-gray-500">Informações de pedigree são opcionais.</p>
            <div>
              <p className="text-xs font-bold text-gray-700 mb-2">Pai</p>
              <div className="grid grid-cols-2 gap-3">
                <div><Label>Nome do pai</Label><Input value={form.nomePai} onChange={v => set('nomePai', v)} placeholder="Nome" /></div>
                <div><Label>Raça do pai</Label><Input value={form.racaPai} onChange={v => set('racaPai', v)} placeholder="Raça" /></div>
              </div>
            </div>
            <div>
              <p className="text-xs font-bold text-gray-700 mb-2">Mãe</p>
              <div className="grid grid-cols-2 gap-3">
                <div><Label>Nome da mãe</Label><Input value={form.nomeMae} onChange={v => set('nomeMae', v)} placeholder="Nome" /></div>
                <div><Label>Raça da mãe</Label><Input value={form.racaMae} onChange={v => set('racaMae', v)} placeholder="Raça" /></div>
              </div>
            </div>
          </div>
        )}

        {passo === 3 && (
          <div className="space-y-4">
            <div className="bg-white rounded-2xl p-4 border border-gray-100 space-y-2">
              <h3 className="font-bold text-sm text-gray-900">Resumo do cadastro</h3>
              <div className="text-sm space-y-1">
                <p><span className="text-gray-500">Nome:</span> <strong>{form.nome}</strong></p>
                <p><span className="text-gray-500">Espécie:</span> {ESPECIE_EMOJI[form.especie]} {ESPECIE_LABEL[form.especie]}</p>
                <p><span className="text-gray-500">Raça:</span> {form.raca}</p>
                <p><span className="text-gray-500">Sexo:</span> {form.sexo === 'femea' ? '♀ Fêmea' : '♂ Macho'}</p>
                {form.peso && <p><span className="text-gray-500">Peso:</span> {form.peso} kg</p>}
                {form.fazenda && <p><span className="text-gray-500">Fazenda:</span> {form.fazenda}</p>}
                <p><span className="text-gray-500">GeneMatch:</span> {form.disponivelMatch ? '✅ Sim' : '❌ Não'}</p>
              </div>
            </div>
            {erro && <p className="text-red-600 text-sm bg-red-50 p-3 rounded-xl">{erro}</p>}
          </div>
        )}
      </div>

      {/* Footer botão */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 p-4">
        <div className="max-w-lg mx-auto">
          <button
            onClick={() => passo < 3 ? setPasso(p => p + 1) : salvar()}
            disabled={saving || (passo === 1 && (!form.nome || !form.raca))}
            className="w-full py-3.5 bg-green-800 hover:bg-green-700 text-white font-bold rounded-xl flex items-center justify-center gap-2 disabled:opacity-50 transition"
          >
            {saving ? (
              <span className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : passo === 3 ? (
              <><Check className="w-4 h-4" /> CONFIRMAR CADASTRO</>
            ) : (
              <>PRÓXIMO <ChevronRight className="w-4 h-4" /></>
            )}
          </button>
        </div>
      </div>
    </div>
  )
}
