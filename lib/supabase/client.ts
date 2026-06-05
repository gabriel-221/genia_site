'use client'
import { createClient as createSupabaseClient, SupabaseClient } from '@supabase/supabase-js'

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL!
const projectRef = supabaseUrl.split('//')[1]?.split('.')[0] ?? ''
const STORAGE_KEY = `sb-${projectRef}-auth-token`

// Cookie-based storage so middleware can verify auth server-side
const cookieStorage = {
  getItem: (key: string): string | null => {
    if (typeof document === 'undefined') return null
    const escaped = key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    const match = document.cookie.match(new RegExp('(?:^|; )' + escaped + '=([^;]*)'))
    return match ? decodeURIComponent(match[1]) : null
  },
  setItem: (key: string, value: string) => {
    if (typeof document === 'undefined') return
    const secure = location.protocol === 'https:' ? '; Secure' : ''
    document.cookie = `${key}=${encodeURIComponent(value)}; path=/; max-age=604800; SameSite=Lax${secure}`
  },
  removeItem: (key: string) => {
    if (typeof document === 'undefined') return
    document.cookie = `${key}=; path=/; max-age=0; SameSite=Lax`
  },
}

let instance: SupabaseClient | null = null

export function createClient(): SupabaseClient {
  if (instance) return instance
  instance = createSupabaseClient(
    supabaseUrl,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
    { auth: { storage: cookieStorage, storageKey: STORAGE_KEY } },
  )
  return instance
}
