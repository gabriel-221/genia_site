import type { Metadata, Viewport } from 'next'
import localFont from 'next/font/local'
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
  icons: {
    icon: [
      { url: '/favicon.ico',       sizes: 'any' },
      { url: '/favicon-16x16.png', sizes: '16x16', type: 'image/png' },
      { url: '/favicon-32x32.png', sizes: '32x32', type: 'image/png' },
    ],
    apple: [
      { url: '/apple-touch-icon.png', sizes: '180x180', type: 'image/png' },
    ],
    other: [
      { rel: 'android-chrome', url: '/android-chrome-192x192.png', sizes: '192x192' },
      { rel: 'android-chrome', url: '/android-chrome-512x512.png', sizes: '512x512' },
    ],
  },
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

// Script VLibras como string — injetado direto no HTML sem passar pelo React
const vlibrasScript = `
(function() {
  if (document.querySelector('[vw]')) return;
  var c = document.createElement('div');
  c.setAttribute('vw', '');
  c.className = 'enabled';
  c.innerHTML = '<div vw-access-button class="active"></div><div vw-plugin-wrapper><div class="vw-plugin-top-wrapper"></div></div>';
  document.body.appendChild(c);
  var s = document.createElement('script');
  s.src = 'https://vlibras.gov.br/app/vlibras-plugin.js';
  s.onload = function() {
    if (window.VLibras) new window.VLibras.Widget('https://vlibras.gov.br/app');
  };
  document.body.appendChild(s);
})();
`

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR">
      <head>
        <meta name="apple-mobile-web-app-capable" content="yes" />
        <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
        <meta name="apple-mobile-web-app-title" content="GENIA" />
        <link rel="icon" type="image/x-icon"  href="/favicon.ico" />
        <link rel="icon" type="image/png" sizes="16x16" href="/favicon-16x16.png" />
        <link rel="icon" type="image/png" sizes="32x32" href="/favicon-32x32.png" />
        <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png" />
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

        {/*
          VLibras — Tradutor de Língua Brasileira de Sinais
          Inline script sem intermediário React — garante execução
          independente do ciclo de vida dos componentes.
        */}
        <script dangerouslySetInnerHTML={{ __html: vlibrasScript }} />
      </body>
    </html>
  )
}
