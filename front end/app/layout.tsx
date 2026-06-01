import type { Metadata } from "next";
import { AppShell } from "@/components/app-shell";
import "./globals.css";

export const metadata: Metadata = {
  title: "Painel Genetico Rural",
  description: "Cadastro de animais, previsao de prenhez e score genetico para decisao do produtor.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR">
      <body>
        <AppShell>{children}</AppShell>
      </body>
    </html>
  );
}
