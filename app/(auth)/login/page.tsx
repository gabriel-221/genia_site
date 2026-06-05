'use client'
import { useState } from 'react'
import { createClient } from '@/lib/supabase/client'
import { Lock, Mail, LogIn } from '@/components/Icons'

export default function LoginPage() {
  const supabase = createClient()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [loading, setLoading] = useState(false)
  const [erro, setErro] = useState('')

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault()
    if (!email || !senha) { setErro('Preencha e-mail e senha.'); return }
    setLoading(true); setErro('')
    const { error } = await supabase.auth.signInWithPassword({ email: email.trim(), password: senha })
    setLoading(false)
    if (error) {
      setErro(
        error.message.includes('Invalid') ? 'E-mail ou senha incorretos.' :
        error.message.includes('confirmed') ? 'Confirme seu e-mail antes de entrar.' :
        'Erro ao entrar. Tente novamente.'
      )
    } else {
      // Força navegação completa para garantir que o estado de auth seja lido
      window.location.href = '/dashboard'
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-green-900 via-green-800 to-green-950 flex flex-col items-center justify-center px-6 py-12">
      {/* Logo */}
      <div className="flex flex-col items-center mb-10">
        <div className="w-20 h-20 bg-white rounded-3xl flex items-center justify-center shadow-2xl mb-4">
          <span className="font-black text-green-800 text-3xl">G</span>
        </div>
        <h1 className="text-4xl font-black text-white tracking-widest">GENIA</h1>
        <p className="text-green-300 text-sm mt-1">Genética Inteligente para o Agro</p>
      </div>

      {/* Card */}
      <div className="w-full max-w-sm bg-white rounded-3xl shadow-2xl p-8">
        <h2 className="text-2xl font-bold text-gray-900 mb-1">Entrar</h2>
        <p className="text-gray-500 text-sm mb-6">Acesse sua conta para gerenciar seu rebanho</p>

        <form onSubmit={handleLogin} className="space-y-4">
          <div className="relative">
            <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-green-700 w-4 h-4" />
            <input
              type="email"
              placeholder="E-mail"
              value={email}
              onChange={e => { setEmail(e.target.value); setErro('') }}
              className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-green-600 focus:ring-1 focus:ring-green-600 bg-gray-50"
            />
          </div>

          <div className="relative">
            <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-green-700 w-4 h-4" />
            <input
              type="password"
              placeholder="Senha"
              value={senha}
              onChange={e => { setSenha(e.target.value); setErro('') }}
              className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-green-600 focus:ring-1 focus:ring-green-600 bg-gray-50"
            />
          </div>

          {erro && (
            <div className="bg-red-50 border border-red-200 rounded-xl px-4 py-3 text-red-700 text-sm">
              {erro}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 bg-green-800 hover:bg-green-700 active:bg-green-900 text-white font-bold rounded-xl flex items-center justify-center gap-2 transition disabled:opacity-60"
          >
            {loading ? (
              <span className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <><LogIn className="w-4 h-4" /> ENTRAR</>
            )}
          </button>
        </form>
      </div>

      {/* LGPD */}
      <div className="mt-6 w-full max-w-sm text-center px-2">
        <p className="text-green-600 text-[10px] leading-relaxed">
          Os dados coletados são tratados conforme a{' '}
          <strong className="text-green-500">Lei 13.709/2018 (LGPD)</strong> e utilizados
          exclusivamente para gestão do rebanho. O produtor é titular e responsável pelos
          seus dados. Armazenamento seguro com isolamento por produtor via Row Level Security.
        </p>
      </div>

      <p className="text-green-700 text-xs mt-4">GNU GPL v3.0 · GENIA 2026</p>
    </div>
  )
}
