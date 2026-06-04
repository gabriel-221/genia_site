'use client'
import { useEffect } from 'react'

export default function VLibras() {
  useEffect(() => {
    // Cria a estrutura HTML exigida pela documentação VLibras via DOM imperativo.
    // Não usamos JSX porque React filtra atributos customizados (vw, vw-access-button).
    // Ref: https://vlibras.gov.br/doc/widget/installation/webpageintegration.html
    const container = document.createElement('div')
    container.setAttribute('vw', '')
    container.className = 'enabled'
    container.innerHTML = `
      <div vw-access-button class="active"></div>
      <div vw-plugin-wrapper>
        <div class="vw-plugin-top-wrapper"></div>
      </div>
    `
    document.body.appendChild(container)

    // Carrega o script e inicializa o widget após o DOM estar pronto
    const script = document.createElement('script')
    script.src = 'https://vlibras.gov.br/app/vlibras-plugin.js'
    script.async = true
    script.onload = () => {
      if ((window as any).VLibras) {
        new (window as any).VLibras.Widget('https://vlibras.gov.br/app')
      }
    }
    document.body.appendChild(script)

    return () => {
      if (container.parentNode) document.body.removeChild(container)
      if (script.parentNode) document.body.removeChild(script)
    }
  }, [])

  return null
}
