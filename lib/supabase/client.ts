'use client'
import { createClient as createSupabaseClient, SupabaseClient } from '@supabase/supabase-js'

// Supabase usa localStorage por padrão — isso garante que o JWT seja
// incluído corretamente em todas as requisições e que o RLS funcione.
// O middleware de rota usa um cookie leve separado (genia-session).

let instance: SupabaseClient | null = null

export function createClient(): SupabaseClient {
  if (instance) return instance
  instance = createSupabaseClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
  )
  return instance
}
