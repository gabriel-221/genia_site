import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

// Cookie leve definido pelo login/logout da aplicação.
// O JWT de sessão real fica em localStorage (gerenciado pelo Supabase JS),
// mas o middleware não tem acesso ao localStorage — por isso usamos
// um cookie de presença separado para proteger as rotas no servidor.
const SESSION_COOKIE = 'genia-session'

export function middleware(request: NextRequest) {
  const cookie = request.cookies.get(SESSION_COOKIE)
  if (!cookie?.value) {
    return NextResponse.redirect(new URL('/login', request.url))
  }
  return NextResponse.next()
}

export const config = {
  matcher: [
    '/dashboard/:path*',
    '/animais/:path*',
    '/match/:path*',
    '/copilot/:path*',
    '/mais/:path*',
    '/relatorios/:path*',
    '/prenhez/:path*',
    '/calendario/:path*',
  ],
}
