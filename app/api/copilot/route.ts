import { NextRequest, NextResponse } from 'next/server'
import { createClient } from '@supabase/supabase-js'

// Best-effort in-memory rate limiter (per user, per serverless instance)
const rateMap = new Map<string, { count: number; reset: number }>()
const RATE_LIMIT = 20
const WINDOW_MS = 60_000

function isRateLimited(userId: string): boolean {
  const now = Date.now()
  const entry = rateMap.get(userId)
  if (!entry || now > entry.reset) {
    rateMap.set(userId, { count: 1, reset: now + WINDOW_MS })
    return false
  }
  if (entry.count >= RATE_LIMIT) return true
  entry.count++
  return false
}

const MAX_MSG     = 1_000
const MAX_RESUMO  = 2_000
const MAX_HISTORY = 10
const MAX_MSG_LEN = 500

export async function POST(req: NextRequest) {
  // 1. Auth — require valid Supabase session token
  const token = req.headers.get('authorization')?.replace(/^Bearer\s+/i, '')
  if (!token) {
    return NextResponse.json({ error: 'Não autorizado.' }, { status: 401 })
  }

  const supabase = createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
  )
  const { data: { user }, error: authError } = await supabase.auth.getUser(token)
  if (authError || !user) {
    return NextResponse.json({ error: 'Sessão inválida. Faça login novamente.' }, { status: 401 })
  }

  // 2. Rate limit by authenticated user ID
  if (isRateLimited(user.id)) {
    return NextResponse.json(
      { error: 'Muitas requisições. Aguarde um momento.' },
      { status: 429 },
    )
  }

  // 3. Parse + sanitize inputs
  const body = await req.json().catch(() => null)
  if (!body?.mensagem) {
    return NextResponse.json({ error: 'mensagem required' }, { status: 400 })
  }

  const mensagem  = String(body.mensagem).slice(0, MAX_MSG)
  const resumo    = body.resumoRebanho
    ? String(body.resumoRebanho).slice(0, MAX_RESUMO)
    : 'Nenhum dado disponível'
  const historico = Array.isArray(body.historico)
    ? body.historico
        .slice(-MAX_HISTORY)
        .map((m: unknown) => {
          if (typeof m !== 'object' || m === null) return null
          const msg = m as Record<string, unknown>
          const role = msg.role === 'assistant' ? 'assistant' : 'user'
          const content = String(msg.content ?? '').slice(0, MAX_MSG_LEN)
          return { role, content }
        })
        .filter(Boolean)
    : []

  const apiKey = process.env.ANTHROPIC_API_KEY
  if (!apiKey) {
    return NextResponse.json({ error: 'API key not configured' }, { status: 500 })
  }

  const systemPrompt = `Você é o GENIA Copilot, assistente inteligente especializado em manejo reprodutivo e genético de rebanhos bovinos, ovinos e caprinos.

=== DADOS DO REBANHO ===
${resumo}

=== INSTRUÇÕES ===
- Responda SEMPRE em português brasileiro
- Seja direto e prático, como um zootecnista experiente
- Use os dados acima para personalizar as respostas
- NÃO use formatação Markdown: sem asteriscos, hashtags, underlines ou backticks
- Responda apenas com texto puro e parágrafos simples
- Máximo 3 parágrafos por resposta
- Em caso de dúvida sobre saúde animal, recomende consulta veterinária`

  const messages = [...historico, { role: 'user', content: mensagem }]

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
      console.error('Claude API error:', res.status, err)
      return NextResponse.json(
        { error: `Erro na IA (${res.status}). Tente novamente.` },
        { status: res.status },
      )
    }

    const data = await res.json()
    const text = data.content?.[0]?.text ?? 'Erro ao processar resposta.'
    return NextResponse.json({ resposta: text })
  } catch (e) {
    console.error('Copilot fetch error:', e)
    return NextResponse.json({ error: 'Erro de conexão com a IA.' }, { status: 500 })
  }
}
