'use client'
import { createClient as createSupabaseClient, SupabaseClient } from '@supabase/supabase-js'

// Singleton para evitar múltiplas instâncias do GoTrueClient
let instance: SupabaseClient | null = null

export function createClient(): SupabaseClient {
  if (instance) return instance
  instance = createSupabaseClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
  )
  return instance
}
