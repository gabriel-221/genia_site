'use client'
import type { ProdutorPublico } from '@/types'

export default function OwnerCard({ produtor }: { produtor: ProdutorPublico }) {
  return (
    <div className="bg-white rounded-3xl shadow-sm p-5">
      <h2 className="text-xs font-black text-gray-400 uppercase tracking-widest mb-3">
        Propriedade
      </h2>
      <div className="space-y-2.5">
        {produtor.nome_fazenda && (
          <div className="flex items-center gap-3">
            <span className="text-xl leading-none select-none">🏡</span>
            <span className="text-sm font-semibold text-gray-700">{produtor.nome_fazenda}</span>
          </div>
        )}
        <div className="flex items-center gap-3">
          <span className="text-xl leading-none select-none">📍</span>
          <span className="text-sm text-gray-600">
            {produtor.municipio}, {produtor.estado}
          </span>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-xl leading-none select-none">👤</span>
          <span className="text-sm text-gray-600">{produtor.nome}</span>
        </div>
      </div>
    </div>
  )
}
