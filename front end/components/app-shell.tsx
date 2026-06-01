"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const links = [
  { href: "/cadastro", label: "Cadastro" },
  { href: "/previsao-cruzamento", label: "Previsao de cruzamento" },
  { href: "/painel-genetico", label: "Painel genetico" },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();

  return (
    <>
      <header className="topbar">
        <div className="topbar-inner">
          <div className="brand-block">
            <span className="brand-kicker">Gestao reprodutiva</span>
            <strong>Painel Genetico Rural</strong>
          </div>

          <nav className="topnav" aria-label="Navegacao principal">
            {links.map((link) => {
              const isActive = pathname === link.href;
              return (
                <Link
                  key={link.href}
                  className={`topnav-link ${isActive ? "active" : ""}`}
                  href={link.href}
                >
                  {link.label}
                </Link>
              );
            })}
          </nav>
        </div>
      </header>

      {children}
    </>
  );
}
