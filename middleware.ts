import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

// Autenticação gerenciada client-side em cada página via supabase.auth.getUser().
// A proteção real dos dados é feita pelo Row Level Security (RLS) do Supabase
// no banco de dados — sem autenticação válida, nenhuma query retorna dados.
export function middleware(_request: NextRequest) {
  return NextResponse.next()
}

export const config = {
  matcher: [],
}
