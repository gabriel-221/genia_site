import { createClient as createSupabaseClient } from '@supabase/supabase-js'

// Server-side client usando service role para API routes
export function createClient() {
  return createSupabaseClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
  )
}
