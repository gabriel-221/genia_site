export default function HomePage() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-green-800 to-green-950 flex flex-col items-center justify-center px-6">
      <div className="text-center text-white">
        <div className="w-20 h-20 bg-yellow-400 rounded-3xl flex items-center justify-center mx-auto mb-6 shadow-2xl">
          <span className="font-black text-green-800 text-3xl">G</span>
        </div>
        <h1 className="text-4xl font-black tracking-tight mb-2">G.E.N.I.A</h1>
        <p className="text-green-300 text-sm mb-2">
          Gestão Evolutiva e Neogênica de Inseminação e IA
        </p>
        <p className="text-green-400 text-xs max-w-xs mx-auto mt-6 leading-relaxed">
          Aproxime seu celular da tag NFC do animal para ver o perfil completo com rastreabilidade
          genética e reprodutiva.
        </p>
      </div>
      <div className="mt-16 text-green-700 text-xs">GNU GPL v3.0 · ProjetAR-CE</div>
    </div>
  )
}
