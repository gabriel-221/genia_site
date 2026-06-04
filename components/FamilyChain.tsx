'use client'
import type { Animal } from '@/types'
import { especieEmoji } from '@/lib/utils'

type AnimalSlim = Pick<Animal, 'id' | 'nome' | 'especie' | 'sexo'>

interface Props {
  animal: Animal
  pai: AnimalSlim | null
  mae: AnimalSlim | null
  filhos: AnimalSlim[]
}

function AnimalChip({
  animal,
  highlight = false,
  label,
}: {
  animal: AnimalSlim
  highlight?: boolean
  label?: string
}) {
  return (
    <div className="flex flex-col items-center gap-1.5 min-w-[72px]">
      {label && <span className="text-xs text-gray-400 font-medium">{label}</span>}
      <div
        className={`w-14 h-14 rounded-2xl flex items-center justify-center text-2xl shadow-sm transition-transform ${
          highlight
            ? 'bg-green-700 ring-2 ring-green-600 ring-offset-2 scale-110'
            : 'bg-gray-100'
        }`}
      >
        <span className="select-none">{especieEmoji(animal.especie)}</span>
      </div>
      <p
        className={`text-xs text-center font-semibold max-w-[72px] truncate leading-tight ${
          highlight ? 'text-green-700' : 'text-gray-600'
        }`}
      >
        {animal.nome}
      </p>
      <p className="text-xs text-gray-400 leading-none">
        {animal.sexo === 'femea' ? '♀' : '♂'}
      </p>
    </div>
  )
}

function Connector() {
  return (
    <div className="flex items-center self-center pb-4">
      <div className="w-5 h-px bg-gray-200" />
      <span className="text-gray-200 text-xs ml-0.5">▶</span>
    </div>
  )
}

export default function FamilyChain({ animal, pai, mae, filhos }: Props) {
  if (!pai && !mae && filhos.length === 0) return null

  return (
    <div className="bg-white rounded-3xl shadow-sm p-5 animate-fade-in">
      <h2 className="text-xs font-black text-gray-400 uppercase tracking-widest mb-4">
        Família
      </h2>

      {/* Parents section */}
      {(pai || mae) && (
        <div className="mb-5">
          <p className="text-xs text-gray-400 mb-3">Pais</p>
          <div className="flex items-start gap-3 overflow-x-auto pb-1">
            {pai && <AnimalChip animal={pai} label="Pai" />}
            {mae && (
              <>
                {pai && <span className="text-gray-300 self-center pb-4 text-sm font-bold">+</span>}
                <AnimalChip animal={mae} label="Mãe" />
              </>
            )}
            <Connector />
            <AnimalChip animal={animal} label="Este" highlight />
          </div>
        </div>
      )}

      {/* Children section */}
      {filhos.length > 0 && (
        <div>
          <p className="text-xs text-gray-400 mb-3">
            Filhos <span className="text-gray-300">({filhos.length})</span>
          </p>
          <div className="flex items-start gap-3 overflow-x-auto pb-1">
            <AnimalChip animal={animal} label="Este" highlight />
            <Connector />
            {filhos.slice(0, 4).map((filho, i) => (
              <AnimalChip key={filho.id ?? i} animal={filho} />
            ))}
            {filhos.length > 4 && (
              <div className="flex flex-col items-center gap-1.5 min-w-[56px] self-start mt-6">
                <div className="w-14 h-14 rounded-2xl bg-gray-100 flex items-center justify-center">
                  <span className="text-xs text-gray-500 font-black">+{filhos.length - 4}</span>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
