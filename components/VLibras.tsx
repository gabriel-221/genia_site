'use client'
import { useEffect } from 'react'
import Script from 'next/script'

export default function VLibras() {
  // Inicializa o widget após o script carregar
  useEffect(() => {
    function init() {
      if (typeof window !== 'undefined' && (window as any).VLibras) {
        new (window as any).VLibras.Widget('https://vlibras.gov.br/app')
      }
    }
    // Tenta imediatamente (se script já carregou) ou aguarda
    if ((window as any).VLibras) {
      init()
    }
  }, [])

  return (
    <>
      {/*
        Estrutura exata exigida pela documentação oficial do VLibras:
        https://vlibras.gov.br/doc/widget/installation/webpageintegration.html

        O atributo "vw" na div externa é obrigatório para o widget funcionar.
        Usamos dangerouslySetInnerHTML para garantir que os atributos HTML
        customizados (vw, vw-access-button, vw-plugin-wrapper) sejam
        renderizados corretamente, sem filtros do React/TypeScript.
      */}
      <div
        dangerouslySetInnerHTML={{
          __html: `
            <div vw class="enabled">
              <div vw-access-button class="active"></div>
              <div vw-plugin-wrapper>
                <div class="vw-plugin-top-wrapper"></div>
              </div>
            </div>
          `,
        }}
      />

      <Script
        src="https://vlibras.gov.br/app/vlibras-plugin.js"
        strategy="afterInteractive"
        onLoad={() => {
          if (typeof window !== 'undefined' && (window as any).VLibras) {
            new (window as any).VLibras.Widget('https://vlibras.gov.br/app')
          }
        }}
      />
    </>
  )
}
