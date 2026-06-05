'use client'
import { createClient as createSupabaseClient, SupabaseClient } from '@supabase/supabase-js'

let instance: SupabaseClient | null = null

export function createClient(): SupabaseClient {
  if (instance) return instance

  instance = createSupabaseClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
  )

  // Quando o token expira ou é inválido, limpa a sessão corrompida e
  // redireciona para login automaticamente (evita o loop de refresh token).
  instance.auth.onAuthStateChange((event) => {
    if (event === 'SIGNED_OUT' && typeof window !== 'undefined') {
      const isPublic = window.location.pathname.startsWith('/a/') ||
                       window.location.pathname.startsWith('/login') ||
                       window.location.pathname === '/'
      if (!isPublic) {
        window.location.href = '/login'
      }
    }
  })

  return instance
}

// Limpa a sessão corrompida e redireciona para login.
// Chame quando receber erro de refresh token inválido.
export async function clearSessionAndRedirect(): Promise<void> {
  if (typeof window === 'undefined') return
  try {
    await instance?.auth.signOut({ scope: 'local' })
  } finally {
    localStorage.clear()
    window.location.href = '/login'
  }
}
