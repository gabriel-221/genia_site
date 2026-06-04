package com.genoboi.data.remote

import android.util.Log
import com.genoboi.BuildConfig
import com.genoboi.domain.model.Animal
import com.genoboi.domain.model.Especie
import com.genoboi.domain.model.Sexo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiCopilotRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun enviarMensagem(
        mensagem: String,
        historico: List<Pair<String, String>>,
        animais: List<Animal>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = buildSystemPrompt(animais)
            val messages = JSONArray()

            historico.forEach { (role, content) ->
                messages.put(JSONObject().apply {
                    put("role", role)
                    put("content", content)
                })
            }
            messages.put(JSONObject().apply {
                put("role", "user")
                put("content", mensagem)
            })

            val body = JSONObject().apply {
                put("model", "claude-3-haiku-20240307")
                put("max_tokens", 1024)
                put("system", systemPrompt)
                put("messages", messages)
            }.toString()

            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", BuildConfig.CLAUDE_API_KEY.ifEmpty { "placeholder_key" })
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("Copilot", "Erro API: ${response.code} $responseBody")
                val errorMsg = if (BuildConfig.CLAUDE_API_KEY.isEmpty()) 
                    "Configuração incompleta: Chave da IA não encontrada no arquivo local.properties."
                else "Erro na IA (${response.code}). Tente novamente mais tarde."
                return@withContext Result.failure(Exception(errorMsg))
            }

            val text = JSONObject(responseBody)
                .getJSONArray("content")
                .getJSONObject(0)
                .getString("text")

            Result.success(text)
        } catch (e: Exception) {
            Log.e("Copilot", "Falha: ${e.message}")
            Result.failure(e)
        }
    }

    private fun buildSystemPrompt(animais: List<Animal>): String {
        val total = animais.size
        val bovinos = animais.count { it.especie == Especie.BOVINO }
        val ovinos = animais.count { it.especie == Especie.OVINO }
        val caprinos = animais.count { it.especie == Especie.CAPRINO }
        val femeas = animais.count { it.sexo == Sexo.FEMEA }
        val machos = animais.count { it.sexo == Sexo.MACHO }
        val prenhas = animais.count { it.prenhou }
        val disponivelMatch = animais.count { it.disponivelMatch }

        val topAnimais = animais.take(10).joinToString("\n") { a ->
            "- ${a.nome} (${a.especie.label}, ${a.raca}, ${a.sexo.label}, ${a.pesoKg.toInt()}kg" +
            if (a.sexo == Sexo.FEMEA && a.producaoLeiteDiaria > 0) ", ${a.producaoLeiteDiaria}L/dia)" else ")"
        }

        return """
Você é o GENIA Copilot, assistente inteligente especializado em manejo reprodutivo e genético de rebanhos.
Você tem acesso aos dados reais do produtor e deve dar respostas práticas, objetivas e em português brasileiro.

=== DADOS DO REBANHO ===
Total de animais: $total
├── Bovinos: $bovinos | Ovinos: $ovinos | Caprinos: $caprinos
├── Fêmeas: $femeas | Machos: $machos
├── Prenhas: $prenhas
└── Disponíveis para GeneMatch: $disponivelMatch

Primeiros animais cadastrados:
$topAnimais

=== INSTRUÇÕES ===
- Responda SEMPRE em português brasileiro.
- Seja direto e prático, como um zootecnista experiente.
- Use os dados acima para personalizar as respostas.
- Se perguntado sobre um animal específico, use os dados fornecidos.
- IMPORTANTE: Não use nenhuma formatação Markdown (como asteriscos para negrito, hashtags para títulos ou listas com hifens). Responda apenas com texto puro, limpo e parágrafos simples.
- Para cálculos, mostre o raciocínio de forma textual e simples.
- Em caso de dúvida sobre saúde animal, recomende consulta veterinária.
        """.trimIndent()
    }
}
