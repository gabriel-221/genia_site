'use client'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { LayoutDashboard, PawPrint, Dna, Sparkles, Users } from '@/components/Icons'

// 4 principais + "Mais" (acessa relatórios, prenhez, calendário, perfil, etc.)
const NAV = [
  { href: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { href: '/animais',   icon: PawPrint,         label: 'Animais'   },
  { href: '/match',     icon: Dna,              label: 'Match'     },
  { href: '/copilot',   icon: Sparkles,         label: 'Copilot'   },
  { href: '/mais',      icon: Users,            label: 'Mais'      },
]

export default function AppLayout({ children }: { children: React.ReactNode }) {
  const path = usePathname()

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <main className="flex-1 pb-20 overflow-auto">
        {children}
      </main>

      {/* Bottom Navigation — 4 funções + Mais */}
      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 shadow-lg z-50" role="navigation" aria-label="Navegação principal">
        <div className="max-w-lg mx-auto flex">
          {NAV.map(({ href, icon: Icon, label }) => {
            // "Mais" fica ativo em qualquer rota não coberta pelos outros 4
            const mainRoutes = ['/dashboard', '/animais', '/match', '/copilot']
            const active = href === '/mais'
              ? !mainRoutes.some(r => path.startsWith(r))
              : path.startsWith(href)

            return (
              <Link
                key={href}
                href={href}
                aria-label={label}
                aria-current={active ? 'page' : undefined}
                className={`flex-1 flex flex-col items-center py-2 gap-0.5 transition-colors ${
                  active ? 'text-green-800' : 'text-gray-400'
                }`}
              >
                <Icon className={`w-5 h-5 ${active ? 'stroke-[2.5]' : 'stroke-[1.5]'}`} />
                <span className={`text-[10px] font-medium ${active ? 'text-green-800' : 'text-gray-400'}`}>
                  {label}
                </span>
                {active && <div className="w-1 h-1 rounded-full bg-green-800 -mb-1" />}
              </Link>
            )
          })}
        </div>
      </nav>
    </div>
  )
}
