import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

const PROJECT_REF = (process.env.NEXT_PUBLIC_SUPABASE_URL ?? '')
  .split('//')[1]?.split('.')[0] ?? ''
const AUTH_COOKIE = `sb-${PROJECT_REF}-auth-token`

export function middleware(request: NextRequest) {
  const cookie = request.cookies.get(AUTH_COOKIE)
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
