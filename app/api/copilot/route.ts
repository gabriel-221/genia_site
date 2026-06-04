import { NextRequest, NextResponse } from 'next/server'
import { createClient } from '@/lib/supabase/server'

export async function POST(req: NextRequest) {
  const supabase = createClient()
  const { data: { user } } = await supabase.auth.getUser()
  if (!user) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const { mensagem, historico, resumoRebanho } = await req.json()
  if (!mensagem) return NextResponse.json({ error: 'mensagem required' }, { status: 400 })

  const apiKey = process.env.ANTHROPIC_API_KEY
  if (!apiKey) return NextResponse.json({ error: 'API key not configured' }, { status: 500 })

  const systemPrompt = `Você é o GENIA Copilot, assistente inteligente especializado em manejo reprodutivo e genético de rebanhos bovinos, ovinos e caprinos.

=== DADOS DO REBANHO ===
${resumoRebanho || 'Nenhum dado disponível'}

=== INSTRUÇÕES ===
- Responda SEMPRE em português brasileiro
- Seja direto e prático, como um zootecnista experiente
- Use os dados acima para personalizar as respostas
- Para cálculos, mostre o raciocínio passo a passo
- Em caso de dúvida sobre saúde animal, recomende consulta veterinária
- Seja objetivo, máximo 3-4 parágrafos por resposta`

  const messages = [
    ...(historico ?? []),
    { role: 'user', content: mensagem },
  ]

  try {
    const res = await fetch('https://api.anthropic.com/v1/messages', {
      method: 'POST',
      headers: {
        'x-api-key': apiKey,
        'anthropic-version': '2023-06-01',
        'content-type': 'application/json',
      },
      body: JSON.stringify({
        model: 'claude-haiku-4-5-20251001',
        max_tokens: 1024,
        system: systemPrompt,
        messages,
      }),
    })

    if (!res.ok) {
      const err = await res.text()
      return NextResponse.json({ error: err }, { status: res.status })
    }

    const data = await res.json()
    const text = data.content?.[0]?.text ?? 'Erro ao processar resposta.'
    return NextResponse.json({ resposta: text })
  } catch (e) {
    return NextResponse.json({ error: 'Erro de conexão com a IA.' }, { status: 500 })
  }
}
