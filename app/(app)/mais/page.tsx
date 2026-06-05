'use client'
import { useEffect, useState } from 'react'
import Link from 'next/link'
import { createClient } from '@/lib/supabase/client'
import type { Produtor } from '@/types'
import {
  BarChart3, Calendar, Heart, ArrowLeft,
  LogOut, Pencil, Users, Check, Lock
} from '@/components/Icons'

function MenuLink({ href, icon: Icon, label, desc, cor = 'text-green-800' }: {
  href: string; icon: React.FC<{ className?: string }>; label: string; desc: string; cor?: string
}) {
  return (
    <Link href={href} className="flex items-center gap-4 bg-white rounded-2xl p-4 border border-gray-100 shadow-sm active:bg-gray-50">
      <div className={`w-10 h-10 rounded-xl flex items-center justify-center bg-green-50`}>
        <Icon className={`w-5 h-5 ${cor}`} />
      </div>
      <div className="flex-1">
        <p className="font-semibold text-gray-900 text-sm">{label}</p>
        <p className="text-xs text-gray-500">{desc}</p>
      </div>
      <ArrowLeft className="w-4 h-4 text-gray-300 rotate-180" />
    </Link>
  )
}

export default function MaisPage() {
  const supabase = createClient()
  const [produtor, setProdutor] = useState<Produtor | null>(null)
  const [editando, setEditando] = useState(false)
  const [nome, setNome] = useState('')
  const [fazenda, setFazenda] = useState('')
  const [municipio, setMunicipio] = useState('')
  const [telefone, setTelefone] = useState('')
  const [salvando, setSalvando] = useState(false)
  const [salvo, setSalvo] = useState(false)

  useEffect(() => {
    async function load() {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) return
      const { data } = await supabase.from('genia_produtor').select('*').eq('user_id', user.id).single()
      if (data) {
        setProdutor(data)
        setNome(data.nome ?? '')
        setFazenda(data.nome_fazenda ?? '')
        setMunicipio(data.municipio ?? '')
        setTelefone(data.telefone ?? '')
      }
    }
    load()
  }, [])

  async function salvarPerfil() {
    if (!produtor) return
    setSalvando(true)
    await supabase.from('genia_produtor').update({
      nome, nome_fazenda: fazenda, municipio, telefone
    }).eq('id', produtor.id)
    setSalvando(false)
    setSalvo(true)
    setEditando(false)
    setProdutor(p => p ? { ...p, nome, nome_fazenda: fazenda, municipio, telefone } : p)
    setTimeout(() => setSalvo(false), 3000)
  }

  async function handleLogout() {
    await supabase.auth.signOut()
    document.cookie = 'genia-session=; path=/; max-age=0; SameSite=Lax'
    window.location.href = '/login'
  }

  const inputCls = 'w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-green-600 bg-gray-50'

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-green-800 px-5 pt-8 pb-16">
        <div className="max-w-lg mx-auto">
          <p className="text-green-300 text-xs font-semibold uppercase tracking-widest mb-1">GENIA</p>
          <h1 className="text-2xl font-black text-white">{produtor?.nome ?? 'Carregando...'}</h1>
          <p className="text-green-300 text-sm mt-0.5">
            {produtor?.nome_fazenda ?? produtor?.municipio ?? ''}
            {produtor?.municipio ? ` · ${produtor.municipio}, ${produtor.estado}` : ''}
          </p>
        </div>
      </div>

      <div className="max-w-lg mx-auto px-4 -mt-8 pb-8 space-y-4">
        {/* Perfil */}
        <div className="bg-white rounded-2xl p-5 border border-gray-100 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-bold text-gray-900 text-sm">Meu Perfil</h2>
            {!editando && (
              <button onClick={() => setEditando(true)}
                className="flex items-center gap-1.5 text-green-700 text-xs font-semibold">
                <Pencil className="w-3.5 h-3.5" /> Editar
              </button>
            )}
          </div>

          {editando ? (
            <div className="space-y-3">
              <div>
                <label className="text-xs font-semibold text-gray-500 mb-1 block">Nome completo</label>
                <input value={nome} onChange={e => setNome(e.target.value)} className={inputCls} />
              </div>
              <div>
                <label className="text-xs font-semibold text-gray-500 mb-1 block">Nome da fazenda</label>
                <input value={fazenda} onChange={e => setFazenda(e.target.value)} className={inputCls} />
              </div>
              <div>
                <label className="text-xs font-semibold text-gray-500 mb-1 block">Município</label>
                <input value={municipio} onChange={e => setMunicipio(e.target.value)} className={inputCls} />
              </div>
              <div>
                <label className="text-xs font-semibold text-gray-500 mb-1 block">WhatsApp (com DDD)</label>
                <input value={telefone} onChange={e => setTelefone(e.target.value)} placeholder="(88) 99999-9999" className={inputCls} />
              </div>
              <div className="flex gap-3 pt-1">
                <button onClick={() => setEditando(false)}
                  className="flex-1 py-2.5 border border-gray-200 rounded-xl text-sm font-medium text-gray-600">
                  Cancelar
                </button>
                <button onClick={salvarPerfil} disabled={salvando}
                  className="flex-1 py-2.5 bg-green-800 text-white rounded-xl text-sm font-bold disabled:opacity-60 flex items-center justify-center gap-2">
                  {salvando ? <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> : <Check className="w-4 h-4" />}
                  Salvar
                </button>
              </div>
            </div>
          ) : (
            <div className="space-y-2 text-sm">
              {salvo && <p className="text-green-700 text-xs bg-green-50 px-3 py-2 rounded-xl">Perfil atualizado com sucesso!</p>}
              <div className="flex justify-between py-1.5 border-b border-gray-50">
                <span className="text-gray-500">E-mail</span>
                <span className="text-gray-700 text-xs">{produtor?.user_id ? '••••@••••' : '-'}</span>
              </div>
              <div className="flex justify-between py-1.5 border-b border-gray-50">
                <span className="text-gray-500">Fazenda</span>
                <span className="font-medium">{produtor?.nome_fazenda || '-'}</span>
              </div>
              <div className="flex justify-between py-1.5 border-b border-gray-50">
                <span className="text-gray-500">Município</span>
                <span className="font-medium">{produtor?.municipio || '-'}</span>
              </div>
              <div className="flex justify-between py-1.5">
                <span className="text-gray-500">WhatsApp</span>
                <span className="font-medium">{produtor?.telefone || '-'}</span>
              </div>
            </div>
          )}
        </div>

        {/* Outras funções */}
        <div className="space-y-2">
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest px-1">Funções</p>
          <MenuLink href="/relatorios"  icon={BarChart3} label="Relatórios"         desc="Desempenho genético e reprodutivo por espécie" />
          <MenuLink href="/prenhez"     icon={Heart}     label="Simulação de Prenhez" desc="IA preditiva — modelo Random Forest" />
          <MenuLink href="/calendario"  icon={Calendar}  label="Calendário"          desc="Eventos reprodutivos e alertas" />
          <MenuLink href="/match"       icon={Users}     label="GeneMatch"           desc="Conecte com outros produtores para melhoramento" />
        </div>

        {/* LGPD + segurança */}
        <div className="bg-gray-100 rounded-2xl p-4">
          <div className="flex items-center gap-2 mb-2">
            <Lock className="w-4 h-4 text-gray-500" />
            <p className="text-xs font-semibold text-gray-600">Privacidade e Segurança</p>
          </div>
          <p className="text-[11px] text-gray-500 leading-relaxed">
            Seus dados são protegidos pela <strong>Lei 13.709/2018 (LGPD)</strong>.
            Armazenamento isolado por produtor via Row Level Security (RLS) no Supabase.
            Código aberto licenciado sob <strong>GNU GPL v3.0</strong>.
          </p>
        </div>

        {/* Sair */}
        <button onClick={handleLogout}
          className="w-full py-3.5 border-2 border-red-200 text-red-600 font-bold rounded-2xl flex items-center justify-center gap-2 hover:bg-red-50 transition">
          <LogOut className="w-4 h-4" /> Sair da conta
        </button>

        <p className="text-center text-[10px] text-gray-400 pb-2">GENIA · GNU GPL v3.0 · Hackathon Expoagro Crateús 2026</p>
      </div>
    </div>
  )
}
