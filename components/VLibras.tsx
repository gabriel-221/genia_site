'use client'
import Script from 'next/script'

export default function VLibras() {
  return (
    <>
      <div vw-access-button="" className="active" aria-label="Ativar tradutor de Libras VLibras" />
      <div vw-plugin-wrapper="">
        <div className="vw-plugin-top-wrapper" />
      </div>
      <Script
        src="https://vlibras.gov.br/app/vlibras-plugin.js"
        strategy="afterInteractive"
        onLoad={() => {
          // @ts-ignore
          if (typeof window !== 'undefined' && window.VLibras) {
            // @ts-ignore
            new window.VLibras.Widget('https://vlibras.gov.br/app')
          }
        }}
      />
    </>
  )
}
