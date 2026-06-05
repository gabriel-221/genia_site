import type { Animal } from '@/types'
import { especieEmoji, especieLabel, getSpeciesGradient } from '@/lib/utils'

// ECC arc — inspired by the CP arc in Pokémon GO
// Arc spans 270° from lower-left (7:30) to lower-right (4:30) through the top
function EccArc({ ecc }: { ecc: number }) {
  const cx = 120, cy = 130, r = 96
  const C = 2 * Math.PI * r
  const arcFraction = 0.75          // 270° / 360°
  const arcLen = C * arcFraction

  const t = Math.max(0, Math.min(1, (ecc - 1) / 4))   // 0 at ECC=1, 1 at ECC=5
  const fillLen = arcLen * t

  const eccColor = ecc < 2 ? '#EF4444' : ecc < 3.5 ? '#F59E0B' : '#10B981'
  const glowColor = ecc < 2 ? '#FCA5A5' : ecc < 3.5 ? '#FCD34D' : '#6EE7B7'

  // Indicator dot: starts at 135° and travels clockwise 270°
  const indicatorAngleDeg = 135 + t * 270
  const indicatorRad = indicatorAngleDeg * (Math.PI / 180)
  const dotX = cx + r * Math.cos(indicatorRad)
  const dotY = cy + r * Math.sin(indicatorRad)

  // Small 1.0 / 5.0 labels at arc ends (outside the arc, shifted outward)
  const labelR = r + 16
  const s1x = cx + labelR * Math.cos(135 * Math.PI / 180)
  const s1y = cy + labelR * Math.sin(135 * Math.PI / 180)
  const s5x = cx + labelR * Math.cos(45 * Math.PI / 180)
  const s5y = cy + labelR * Math.sin(45 * Math.PI / 180)

  return (
    <svg viewBox="0 0 240 240" className="w-full h-full" aria-hidden="true">
      <defs>
        <filter id="glow">
          <feGaussianBlur stdDeviation="3" result="blur" />
          <feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
        </filter>
      </defs>

      {/* Track — faint white arc */}
      <circle cx={cx} cy={cy} r={r}
        fill="none"
        stroke="rgba(255,255,255,0.15)"
        strokeWidth="6"
        strokeLinecap="round"
        strokeDasharray={`${arcLen} ${C}`}
        transform={`rotate(135, ${cx}, ${cy})`}
      />

      {/* Filled value arc */}
      {t > 0.01 && (
        <circle cx={cx} cy={cy} r={r}
          fill="none"
          stroke={eccColor}
          strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray={`${fillLen} ${C}`}
          transform={`rotate(135, ${cx}, ${cy})`}
          filter="url(#glow)"
          opacity="0.9"
        />
      )}

      {/* Indicator dot */}
      <circle cx={dotX} cy={dotY} r={10} fill={glowColor} opacity="0.35" />
      <circle cx={dotX} cy={dotY} r={7} fill="white" />
      <circle cx={dotX} cy={dotY} r={4.5} fill={eccColor} />

      {/* Arc end labels */}
      <text x={s1x} y={s1y} textAnchor="middle" dominantBaseline="middle"
        fill="rgba(255,255,255,0.35)" fontSize="10" fontWeight="bold" fontFamily="monospace">
        1.0
      </text>
      <text x={s5x} y={s5y} textAnchor="middle" dominantBaseline="middle"
        fill="rgba(255,255,255,0.35)" fontSize="10" fontWeight="bold" fontFamily="monospace">
        5.0
      </text>
    </svg>
  )
}

export default function HeroSection({ animal }: { animal: Animal }) {
  const { from, to } = getSpeciesGradient(animal.especie)
  const ecc = animal.escore_corporal ?? null

  return (
    <div
      className="relative w-full overflow-hidden"
      style={{ background: `linear-gradient(155deg, ${from} 0%, ${to} 100%)`, minHeight: '380px' }}
    >
      {/* Radial depth overlay */}
      <div className="absolute inset-0 pointer-events-none"
        style={{ background: 'radial-gradient(ellipse at 50% 0%, rgba(255,255,255,0.10) 0%, transparent 65%)' }} />

      {/* Floating decoration dots */}
      <span className="absolute w-40 h-40 rounded-full opacity-5 bg-white" style={{ top: '-40px', left: '-40px' }} />
      <span className="absolute w-24 h-24 rounded-full opacity-5 bg-white" style={{ bottom: '20px', right: '-20px' }} />
      <span className="absolute w-2 h-2 rounded-full bg-white/20 animate-pulse" style={{ top: '22%', left: '10%' }} />
      <span className="absolute w-1.5 h-1.5 rounded-full bg-white/15 animate-pulse" style={{ top: '55%', left: '6%', animationDelay: '0.7s' }} />
      <span className="absolute w-2 h-2 rounded-full bg-white/10 animate-pulse" style={{ top: '18%', right: '8%', animationDelay: '1.2s' }} />
      <span className="absolute w-1 h-1 rounded-full bg-white/25 animate-pulse" style={{ top: '65%', right: '12%', animationDelay: '0.4s' }} />

      {/* Top bar */}
      <div className="relative z-10 flex items-center justify-between px-5 py-3">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 bg-white/20 rounded-full flex items-center justify-center backdrop-blur-sm">
            <span className="text-xs font-black text-white">G</span>
          </div>
          <span className="text-white/80 text-sm font-bold tracking-wide">G.E.N.I.A</span>
        </div>
        {animal.rfid_tag && (
          <span className="text-white/40 text-[10px] font-mono bg-black/20 px-2 py-1 rounded-full">
            #{animal.rfid_tag}
          </span>
        )}
      </div>

      {/* ECC label — sits above the arc at the top */}
      <div className="relative z-10 text-center -mb-1">
        <p className="text-white/50 text-[9px] font-black tracking-widest uppercase">
          Escore Corporal
        </p>
        {ecc != null ? (
          <div className="flex items-baseline justify-center gap-1">
            <span className="text-white text-5xl font-black tabular-nums leading-none drop-shadow-sm">
              {ecc.toFixed(1)}
            </span>
            <span className="text-white/40 text-sm font-bold">/5</span>
          </div>
        ) : (
          <p className="text-white/30 text-sm">—</p>
        )}
      </div>

      {/* Arc + Avatar */}
      <div className="relative mx-auto" style={{ width: '240px', height: '220px' }}>
        {/* ECC arc SVG fills the container */}
        {ecc != null && (
          <div className="absolute inset-0">
            <EccArc ecc={ecc} />
          </div>
        )}

        {/* Avatar — centered at arc center (cx=120, cy=130 in 240px viewBox = 120px, 130px) */}
        <div className="absolute" style={{ top: '74px', left: '50%', transform: 'translateX(-50%)' }}>
          <div className="relative">
            {/* Outer glow ring */}
            <div className="absolute -inset-1 rounded-full animate-pulse"
              style={{ background: 'radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%)' }} />

            <div className="w-28 h-28 rounded-full overflow-hidden shadow-2xl relative"
              style={{ border: '3px solid rgba(255,255,255,0.35)', boxShadow: '0 0 0 1px rgba(255,255,255,0.1), 0 20px 40px rgba(0,0,0,0.3)' }}>
              {animal.foto_url ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={animal.foto_url} alt={animal.nome} className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full bg-white/15 flex items-center justify-center">
                  <span className="text-6xl select-none">{especieEmoji(animal.especie)}</span>
                </div>
              )}
              {/* Shine overlay */}
              <div className="absolute inset-0 rounded-full pointer-events-none"
                style={{ background: 'linear-gradient(135deg, rgba(255,255,255,0.15) 0%, transparent 50%)' }} />
            </div>
          </div>
        </div>
      </div>

      {/* Name */}
      <h1 className="relative z-10 text-center text-white text-3xl font-black -mt-3 tracking-wide drop-shadow-lg px-4">
        {animal.nome.toUpperCase()}
      </h1>

      {/* Badges */}
      <div className="relative z-10 flex justify-center flex-wrap gap-2 mt-3 px-5 pb-12">
        <span className="px-3 py-1 bg-white/20 backdrop-blur-sm rounded-full text-white text-xs font-bold shadow-sm">
          {especieEmoji(animal.especie)} {especieLabel(animal.especie)}
        </span>
        {animal.raca && (
          <span className="px-3 py-1 bg-white/20 backdrop-blur-sm rounded-full text-white text-xs font-bold shadow-sm">
            {animal.raca}
          </span>
        )}
        <span className="px-3 py-1 bg-white/20 backdrop-blur-sm rounded-full text-white text-xs font-bold shadow-sm">
          {animal.sexo === 'femea' ? '♀ Fêmea' : '♂ Macho'}
        </span>
        {animal.prenhou && (
          <span className="px-3 py-1 rounded-full text-xs font-bold shadow-sm"
            style={{ background: 'rgba(244,114,182,0.35)', color: '#fce7f3', backdropFilter: 'blur(4px)' }}>
            ✦ Gestante
          </span>
        )}
        {animal.disponivel_match && (
          <span className="px-3 py-1 rounded-full text-xs font-bold shadow-sm"
            style={{ background: 'rgba(52,211,153,0.25)', color: '#d1fae5', backdropFilter: 'blur(4px)' }}>
            ⬡ GeneMatch
          </span>
        )}
      </div>
    </div>
  )
}
