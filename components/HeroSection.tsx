import type { Animal } from '@/types'
import { especieEmoji, especieLabel, getSpeciesGradient } from '@/lib/utils'

interface Props {
  animal: Animal
}

export default function HeroSection({ animal }: Props) {
  const { from, to } = getSpeciesGradient(animal.especie)

  return (
    <div
      className="relative w-full pb-12 pt-safe"
      style={{
        background: `linear-gradient(160deg, ${from} 0%, ${to} 100%)`,
        minHeight: '300px',
      }}
    >
      {/* Top bar */}
      <div className="flex items-center justify-between px-5 py-3">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 bg-white/20 rounded-full flex items-center justify-center">
            <span className="text-xs font-black text-white">G</span>
          </div>
          <span className="text-white/80 text-sm font-bold tracking-wide">G.E.N.I.A</span>
        </div>
        {animal.rfid_tag && (
          <span className="text-white/40 text-xs font-mono">#{animal.rfid_tag}</span>
        )}
      </div>

      {/* Score — like CP in Pokémon GO */}
      {animal.escore_corporal != null && (
        <div className="text-center mt-2">
          <span className="text-white/60 text-xs font-bold tracking-widest uppercase">
            Escore Genia
          </span>
          <div className="flex items-baseline justify-center gap-1 mt-0.5">
            <span className="text-white text-5xl font-black leading-none tabular-nums">
              {animal.escore_corporal.toFixed(1)}
            </span>
            <span className="text-white/50 text-base font-bold">/5</span>
          </div>
        </div>
      )}

      {/* Avatar */}
      <div className="flex justify-center mt-4">
        {animal.foto_url ? (
          <div className="w-28 h-28 rounded-full border-4 border-white/30 overflow-hidden shadow-2xl bg-white/10">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              src={animal.foto_url}
              alt={animal.nome}
              className="w-full h-full object-cover"
            />
          </div>
        ) : (
          <div className="w-28 h-28 rounded-full border-4 border-white/30 bg-white/20 flex items-center justify-center shadow-2xl">
            <span className="text-6xl select-none">{especieEmoji(animal.especie)}</span>
          </div>
        )}
      </div>

      {/* Name */}
      <h1 className="text-center text-white text-3xl font-black mt-3 tracking-wide drop-shadow-sm px-4">
        {animal.nome.toUpperCase()}
      </h1>

      {/* Badges — espécie, raça, sexo */}
      <div className="flex justify-center flex-wrap gap-2 mt-2 px-6">
        <span className="px-3 py-1 bg-white/20 backdrop-blur-sm rounded-full text-white text-xs font-bold">
          {especieLabel(animal.especie)}
        </span>
        {animal.raca && (
          <span className="px-3 py-1 bg-white/20 backdrop-blur-sm rounded-full text-white text-xs font-bold">
            {animal.raca}
          </span>
        )}
        <span className="px-3 py-1 bg-white/20 backdrop-blur-sm rounded-full text-white text-xs font-bold">
          {animal.sexo === 'femea' ? '♀ Fêmea' : '♂ Macho'}
        </span>
      </div>
    </div>
  )
}
