'use client'
import { useEffect } from 'react'

// Variável de controle para evitar inicialização dupla no Next.js
let initialized = false

export default function VLibras() {
  useEffect(() => {
    // Evita inicializar mais de uma vez (Next.js pode montar o layout várias vezes)
    if (initialized) return
    initialized = true

    // 1. Cria a estrutura HTML exata exigida pela documentação VLibras
    //    https://vlibras.gov.br/doc/widget/installation/webpageintegration.html
    //    Feito via DOM imperativo para que os atributos customizados (vw, vw-access-button)
    //    sejam renderizados sem filtragem pelo React.
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

    // 2. Carrega o script VLibras e inicializa o widget
    //    forceOnload: verifica document.readyState e inicializa imediatamente
    //    se a página já carregou (sempre true em SPA/Next.js)
    const script = document.createElement('script')
    script.src = 'https://vlibras.gov.br/app/vlibras-plugin.js'
    script.async = true
    script.onload = () => {
      const win = window as Window & { VLibras?: { Widget: new (url: string) => { init?: () => void } } }
      if (!win.VLibras) return

      // Instancia o widget
      const widget = new win.VLibras.Widget('https://vlibras.gov.br/app')

      // Força inicialização manual (equivalente ao forceOnload da lib vlibras-nextjs)
      // Necessário porque o evento 'load' já disparou antes do script ser inserido
      if (document.readyState === 'complete') {
        if (widget && typeof widget.init === 'function') {
          widget.init()
        } else {
          // Alguns browsers precisam de um tick para o widget se registrar
          setTimeout(() => {
            window.dispatchEvent(new Event('VLibrasLoaded'))
          }, 300)
        }
      } else {
        window.addEventListener('load', () => {
          if (widget && typeof widget.init === 'function') widget.init()
        })
      }
    }

    document.body.appendChild(script)

    // Sem cleanup — o container e o script devem permanecer
    // para o VLibras funcionar durante toda a sessão
  }, [])

  return null
}
