import type { Metadata, Viewport } from 'next'
import localFont from 'next/font/local'
import VLibras from '@/components/VLibras'
import './globals.css'

const geistSans = localFont({
  src: './fonts/GeistVF.woff',
  variable: '--font-geist-sans',
  weight: '100 900',
})

export const metadata: Metadata = {
  title: 'GENIA — Gestão Genética de Rebanhos',
  description: 'Manejo reprodutivo e genético de bovinos, ovinos e caprinos com Inteligência Artificial. Acessível a todos os públicos.',
  manifest: '/manifest.json',
  appleWebApp: {
    capable: true,
    statusBarStyle: 'black-translucent',
    title: 'GENIA',
  },
  other: {
    'mobile-web-app-capable': 'yes',
  },
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  themeColor: '#1F5C36',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR">
      <head>
        <meta name="apple-mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
        <meta name="apple-mobile-web-app-title" content="GENIA" />
        <link rel="apple-touch-icon" href="/favicon.ico" />
      </head>
      <body className={`${geistSans.variable} antialiased`}>
        {/* Skip link — eMAG / WCAG 2.4.1 */}
        <a
          href="#conteudo-principal"
          className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-[9999] focus:bg-green-800 focus:text-white focus:px-4 focus:py-2 focus:rounded-xl focus:text-sm focus:font-bold"
        >
          Pular para o conteúdo principal
        </a>

        <main id="conteudo-principal">
          {children}
        </main>

        {/* VLibras — Tradutor de Libras (Língua Brasileira de Sinais) */}
        <VLibras />
      </body>
    </html>
  )
}
