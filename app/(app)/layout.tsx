'use client'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { LayoutDashboard, PawPrint, Dna, Sparkles, BarChart3, Calendar } from '@/components/Icons'

const NAV = [
  { href: '/dashboard',  icon: LayoutDashboard, label: 'Dashboard' },
  { href: '/animais',    icon: PawPrint,         label: 'Animais'   },
  { href: '/match',      icon: Dna,              label: 'Match'     },
  { href: '/copilot',    icon: Sparkles,         label: 'Copilot'   },
  { href: '/relatorios', icon: BarChart3,        label: 'Relatórios'},
]

export default function AppLayout({ children }: { children: React.ReactNode }) {
  const path = usePathname()

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <main className="flex-1 pb-20 overflow-auto">
        {children}
      </main>

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 shadow-lg z-50">
        <div className="max-w-lg mx-auto flex">
          {NAV.map(({ href, icon: Icon, label }) => {
            const active = path.startsWith(href)
            return (
              <Link key={href} href={href} className={`flex-1 flex flex-col items-center py-2 gap-0.5 transition-colors ${
                active ? 'text-green-800' : 'text-gray-400'
              }`}>
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
