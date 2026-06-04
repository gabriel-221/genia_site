import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

// Autenticação verificada client-side em cada página via supabase.auth.getUser()
// O Supabase JS armazena sessão em localStorage — não acessível no servidor.
export async function middleware(_request: NextRequest) {
  return NextResponse.next()
}

export const config = {
  matcher: [],
}
