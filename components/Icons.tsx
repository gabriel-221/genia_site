// Ícones inline — sem dependência externa
import React from 'react'

type IconProps = { className?: string; size?: number }

function Icon({ d, className = 'w-5 h-5', viewBox = '0 0 24 24' }: { d: string | string[]; className?: string; viewBox?: string }) {
  const paths = Array.isArray(d) ? d : [d]
  return (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox={viewBox} fill="none"
      stroke="currentColor" strokeWidth={1.75} strokeLinecap="round" strokeLinejoin="round"
      className={className}>
      {paths.map((p, i) => <path key={i} d={p} />)}
    </svg>
  )
}

export const LayoutDashboard = ({ className }: IconProps) => <Icon className={className} d={['M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z','M9 22V12h6v10']} />
export const PawPrint = ({ className }: IconProps) => <Icon className={className} d={['M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10z','M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12']} />
export const Dna = ({ className }: IconProps) => <Icon className={className} d={['M2 15c6.667-6 13.333 0 20-6','M2 9c6.667 6 13.333 0 20 6','M2 12h.01','M22 12h.01']} />
export const Sparkles = ({ className }: IconProps) => <Icon className={className} d={['M9.937 15.5A2 2 0 0 0 8.5 14.063l-6.135-1.582a.5.5 0 0 1 0-.962L8.5 9.936A2 2 0 0 0 9.937 8.5l1.582-6.135a.5.5 0 0 1 .963 0L14.063 8.5A2 2 0 0 0 15.5 9.937l6.135 1.581a.5.5 0 0 1 0 .964L15.5 14.063a2 2 0 0 0-1.437 1.437l-1.582 6.135a.5.5 0 0 1-.963 0z']} />
export const BarChart3 = ({ className }: IconProps) => <Icon className={className} d={['M3 3v18h18','M18 17V9','M13 17V5','M8 17v-3']} />
export const Calendar = ({ className }: IconProps) => <Icon className={className} d={['M8 2v4','M16 2v4','M3 10h18','M21 8a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2h14a2 2 0 002-2V8z']} />
export const Plus = ({ className }: IconProps) => <Icon className={className} d={['M12 5v14','M5 12h14']} />
export const Search = ({ className }: IconProps) => <Icon className={className} d={['M21 21l-4.35-4.35','M17 11A6 6 0 115 11a6 6 0 0112 0z']} />
export const ChevronRight = ({ className }: IconProps) => <Icon className={className} d="M9 18l6-6-6-6" />
export const ChevronLeft  = ({ className }: IconProps) => <Icon className={className} d="M15 18l-6-6 6-6" />
export const ArrowLeft    = ({ className }: IconProps) => <Icon className={className} d={['M19 12H5','M12 19l-7-7 7-7']} />
export const LogOut       = ({ className }: IconProps) => <Icon className={className} d={['M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4','M16 17l5-5-5-5','M21 12H9']} />
export const Heart        = ({ className }: IconProps) => <Icon className={className} d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" />
export const Users        = ({ className }: IconProps) => <Icon className={className} d={['M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2','M23 21v-2a4 4 0 00-3-3.87','M9 11a4 4 0 100-8 4 4 0 000 8z','M16 3.13a4 4 0 010 7.75']} />
export const Pencil       = ({ className }: IconProps) => <Icon className={className} d={['M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7','M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z']} />
export const Trash2       = ({ className }: IconProps) => <Icon className={className} d={['M3 6h18','M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6','M10 11v6','M14 11v6','M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2']} />
export const Scale        = ({ className }: IconProps) => <Icon className={className} d={['M16 16l3-8 3 8c-.87.65-1.92 1-3 1s-2.13-.35-3-1z','M2 16l3-8 3 8c-.87.65-1.92 1-3 1s-2.13-.35-3-1z','M7 21h10','M12 3v18','M3 7h2c2 0 5-1 7-2 2 1 5 2 7 2h2']} />
export const Lock         = ({ className }: IconProps) => <Icon className={className} d={['M19 11H5a2 2 0 00-2 2v7a2 2 0 002 2h14a2 2 0 002-2v-7a2 2 0 00-2-2z','M7 11V7a5 5 0 0110 0v4']} />
export const Mail         = ({ className }: IconProps) => <Icon className={className} d={['M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z','M22 6l-10 7L2 6']} />
export const LogIn        = ({ className }: IconProps) => <Icon className={className} d={['M15 3h4a2 2 0 012 2v14a2 2 0 01-2 2h-4','M10 17l5-5-5-5','M15 12H3']} />
export const Send         = ({ className }: IconProps) => <Icon className={className} d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z" />
export const X            = ({ className }: IconProps) => <Icon className={className} d={['M18 6L6 18','M6 6l12 12']} />
export const CheckCircle2 = ({ className }: IconProps) => <Icon className={className} d={['M22 11.08V12a10 10 0 11-5.93-9.14','M22 4L12 14.01l-3-3']} />
export const Check        = ({ className }: IconProps) => <Icon className={className} d="M20 6L9 17l-5-5" />
export const Syringe      = ({ className }: IconProps) => <Icon className={className} d={['M18 2l4 4','M17 7l-1.5-1.5','M19.5 4.5L7 17l-2 4 4-2 12.5-12.5L19.5 4.5z','M2 22l5-2']} />
export const Microscope   = ({ className }: IconProps) => <Icon className={className} d={['M6 18h8','M3 22h18','M14 22a7 7 0 110-14','M9 14L9 4','M7 4h4']} />
export const Baby         = ({ className }: IconProps) => <Icon className={className} d={['M9 12h.01','M15 12h.01','M10 16c.5.3 1.2.5 2 .5s1.5-.2 2-.5','M19 6.3a9 9 0 010 11.4M5 6.3a9 9 0 000 11.4','M12 20.1c3.7 0 6.9-2.6 7.9-6.1']} />
