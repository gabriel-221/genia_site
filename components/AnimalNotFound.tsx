export default function AnimalNotFound({ code }: { code: string }) {
  return (
    <div className="min-h-screen bg-gradient-to-b from-green-800 to-green-950 flex flex-col items-center justify-center px-6">
      <div className="text-center text-white">
        <div className="text-7xl mb-6 select-none">🐄</div>
        <h1 className="text-2xl font-black mb-2">Animal não encontrado</h1>
        <p className="text-green-300 text-sm mb-1">
          Tag:{' '}
          <code className="font-mono bg-white/10 px-2 py-0.5 rounded">{code}</code>
        </p>
        <p className="text-green-400 text-xs mt-4 max-w-xs">
          Este código NFC pode estar desatualizado ou não cadastrado no sistema.
        </p>
        <div className="mt-10 flex items-center justify-center gap-2 text-green-500 text-xs">
          <span className="w-5 h-5 bg-white/10 rounded-full flex items-center justify-center font-black text-white text-xs">
            G
          </span>
          <span className="font-bold text-white">G.E.N.I.A</span>
          <span>·</span>
          <span>Rastreabilidade bovina</span>
        </div>
      </div>
    </div>
  )
}
