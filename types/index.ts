export type Especie = 'bovino' | 'ovino' | 'caprino'
export type Sexo = 'macho' | 'femea'
export type EventoTipo = 'cio' | 'inseminacao' | 'diagnostico' | 'parto' | 'desmame'

export interface Produtor {
  id: string
  user_id: string
  nome: string
  nome_fazenda?: string
  municipio: string
  estado: string
  cpf?: string
  telefone?: string
  hectares?: number
}

export interface ProdutorPublico {
  nome: string
  nome_fazenda?: string
  municipio: string
  estado: string
}

export interface Animal {
  id: string
  produtor_id: string
  nome: string
  especie: Especie
  raca: string
  linhagem?: string
  sexo: Sexo
  data_nascimento?: string
  rfid_tag?: string
  slug_publico?: string
  peso_kg?: number
  escore_corporal?: number
  coef_endogamia?: number
  foto_url?: string
  ativo: boolean
  criado_em: string
  atualizado_em: string
  fazenda: string
  nome_pai: string
  raca_pai: string
  rfid_pai: string
  nome_mae: string
  raca_mae: string
  rfid_mae: string
  numero_partos: number
  abortos: number
  dias_ultimo_parto: number
  filhos_matriz: number
  qualidade_semen: number
  filhos_macho: number
  producao_leite_diaria: number
  chance_prenhez?: number
  prenhou: boolean
  nfc_ativo: boolean
  nfc_gravada_em?: string
  disponivel_match: boolean
  genia_produtor?: ProdutorPublico | null
}

export interface EventoReprodutivo {
  id: string
  animal_id: string
  tipo: EventoTipo
  data_evento: string
  semen_reprodutor: string
  gestacao_confirmada?: boolean
  data_parto_previsto?: string
  score_ia_prenhez?: number
  tecnico_responsavel?: string
  observacoes?: string
}

export interface ScoredAnimal {
  animal: Animal
  scoreFinal: number
  classificacao: string
  fatores: string[]
}

export const RACAS: Record<Especie, string[]> = {
  bovino:  ['Girolando', 'Nelore', 'Gir', 'Angus', 'Holandês'],
  ovino:   ['Dorper', 'Santa Inês', 'Texel', 'Morada Nova', 'Somalis'],
  caprino: ['Boer', 'Anglo-Nubiana', 'Saanen', 'Alpina', 'Toggenburg', 'Moxotó'],
}

export const ESPECIE_EMOJI: Record<Especie, string> = {
  bovino: '🐄', ovino: '🐑', caprino: '🐐',
}

export const ESPECIE_LABEL: Record<Especie, string> = {
  bovino: 'Bovino', ovino: 'Ovino', caprino: 'Caprino',
}
