import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

const APP_ROUTES = ['/dashboard', '/animais', '/match', '/copilot', '/relatorios', '/calendario']

export async function middleware(request: NextRequest) {
  const path = request.nextUrl.pathname
  const isAppRoute = APP_ROUTES.some(r => path.startsWith(r))

  // Verifica token de sessão do Supabase nos cookies
  const hasSession =
    request.cookies.has('sb-access-token') ||
    request.cookies.has('sb-refresh-token') ||
    // Supabase SSR usa este padrão de cookie
    Array.from(request.cookies.getAll()).some(c =>
      c.name.startsWith('sb-') && c.name.endsWith('-auth-token')
    )

  if (isAppRoute && !hasSession) {
    return NextResponse.redirect(new URL('/login', request.url))
  }
  if (path === '/login' && hasSession) {
    return NextResponse.redirect(new URL('/dashboard', request.url))
  }

  return NextResponse.next()
}

export const config = {
  matcher: ['/dashboard/:path*', '/animais/:path*', '/match/:path*',
            '/copilot/:path*', '/relatorios/:path*', '/calendario/:path*', '/login'],
}
